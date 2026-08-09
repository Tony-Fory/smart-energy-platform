package com.smartenergy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartenergy.common.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * JWT 认证过滤器
 * <p>
 * 拦截需要认证的 API 请求，验证 Authorization: Bearer <token> 头。
 * 不需要认证的路径在 excludedPaths 中配置。
 * <p>
 * 注意：不使用 Spring Security，仅作为轻量级请求过滤器。
 *
 * @author smart-energy
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    /**
     * 不需要 JWT 认证的路径
     */
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/auth/login"
    );

    /**
     * 不需要 JWT 的内部数据上报路径（simulator 调用）
     * 说明：采集器认证将在后续真实设备接入阶段设计
     */
    private static final Set<String> INTERNAL_PATHS = Set.of(
            "/api/energy/data"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Swagger / Actuator / 内部上报路径 不需要认证
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")) {
            return true;
        }
        if (EXCLUDED_PATHS.contains(path) || INTERNAL_PATHS.contains(path)) {
            return true;
        }
        return false;
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

            AuthContext.set(new AuthContext.UserInfo(userId, username));
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
        Result<Void> result = Result.error(status.value(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
