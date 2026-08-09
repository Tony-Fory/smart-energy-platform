package com.smartenergy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartenergy.common.Result;
import com.smartenergy.service.PermissionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * JWT 认证过滤器（"你是谁"）
 * <p>
 * 验证 Authorization: Bearer token，解析用户身份，
 * 从数据库加载权限并存入 AuthContext。
 * 权限校验由 PermissionInterceptor 完成。
 *
 * @author smart-energy
 */
@Slf4j
@Component
@Profile("!test")
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;

    /** 不需要 JWT 认证的路径 */
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/auth/login"
    );

    /** 内部数据上报路径（simulator，不要求 JWT） */
    private static final Set<String> INTERNAL_PATHS = Set.of(
            "/api/energy/data"
    );

    public JwtAuthFilter(JwtService jwtService, PermissionService permissionService,
                         ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.permissionService = permissionService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")) {
            return true;
        }
        return EXCLUDED_PATHS.contains(path) || INTERNAL_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, HttpStatus.UNAUTHORIZED, "缺少认证信息");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.parseToken(token);
            String username = claims.getSubject();
            Long userId = claims.get("userId", Long.class);

            // 从数据库加载权限（权限以数据库为准，不写入 JWT）
            Set<String> permissions = permissionService.getUserPermissions(userId);
            String roleCode = permissionService.getUserRoleCode(userId);

            AuthContext.set(new AuthContext.UserInfo(userId, username, roleCode, permissions));
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Token 已过期");
        } catch (JwtException e) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Token 无效");
        } finally {
            AuthContext.clear();
        }
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(Result.error(status.value(), message)));
    }
}
