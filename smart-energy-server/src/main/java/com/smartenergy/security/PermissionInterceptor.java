package com.smartenergy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartenergy.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * 权限拦截器（"你能做什么"）
 * <p>
 * 在 JwtAuthFilter 之后执行，根据请求路径和方法判断用户是否有对应权限。
 * 权限映射：
 * - /api/dashboard/*  → DASHBOARD_VIEW
 * - GET /api/devices   → DEVICE_VIEW
 * - POST /api/devices  → DEVICE_CREATE
 * - PUT /api/devices/* → DEVICE_UPDATE
 * - DELETE /api/devices/* → DEVICE_DELETE
 * - /api/users/*       → USER_VIEW
 *
 * @author smart-energy
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class PermissionInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    /** 路径前缀 → 权限映射（按前缀匹配） */
    private static final Map<String, String> PATH_PERMISSION_MAP = Map.of(
            "/api/dashboard", "DASHBOARD_VIEW",
            "/api/users", "USER_VIEW"
    );

    /** 不需要权限检查的路径 */
    private static final String AUTH_PATH = "/api/auth";
    private static final String ENERGY_DATA_PATH = "/api/energy/data";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 只对 Controller 方法做权限检查
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String path = request.getServletPath();
        String method = request.getMethod();

        // 不需要权限的路径
        if (path.startsWith(AUTH_PATH) || path.startsWith(ENERGY_DATA_PATH)
                || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")) {
            return true;
        }

        AuthContext.UserInfo user = AuthContext.get();
        // 理论上不会到这里（JwtAuthFilter 已拦截），但防御一下
        if (user == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "缺少认证信息");
            return false;
        }

        String requiredPermission = resolvePermission(path, method);
        if (requiredPermission == null) {
            // 未映射的路径默认放行
            return true;
        }

        if (!user.hasPermission(requiredPermission)) {
            log.warn("权限不足: user={}, path={}, required={}, permissions={}",
                    user.getUsername(), path, requiredPermission, user.getPermissions());
            writeError(response, HttpStatus.FORBIDDEN, "无权限访问");
            return false;
        }

        return true;
    }

    /**
     * 根据请求路径和方法解析需要的权限编码
     */
    private String resolvePermission(String path, String method) {
        // 前缀匹配
        for (var entry : PATH_PERMISSION_MAP.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Device 路径按方法区分权限
        if (path.startsWith("/api/devices")) {
            return switch (method) {
                case "POST" -> "DEVICE_CREATE";
                case "PUT" -> "DEVICE_UPDATE";
                case "DELETE" -> "DEVICE_DELETE";
                default -> "DEVICE_VIEW"; // GET
            };
        }

        return null;
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message)
            throws Exception {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(Result.error(status.value(), message)));
    }
}
