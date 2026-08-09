package com.smartenergy.security;

import com.smartenergy.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtService 单元测试
 */
@DisplayName("JwtService 单元测试")
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-for-unit-test-2026-min-length-32");
        props.setExpiration(2); // 2 seconds for testing
        jwtService = new JwtService(props);
    }

    @Test
    @DisplayName("生成 Token 并成功解析")
    void shouldGenerateAndParseToken() {
        String token = jwtService.generateToken("admin", 1L);

        assertNotNull(token);
        Claims claims = jwtService.parseToken(token);
        assertEquals("admin", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("validateToken 对有效 Token 返回 true")
    void shouldValidateValidToken() {
        String token = jwtService.generateToken("admin", 1L);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("validateToken 对非法 Token 返回 false")
    void shouldRejectInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("validateToken 对空字符串返回 false")
    void shouldRejectEmptyToken() {
        assertFalse(jwtService.validateToken(""));
    }

    @Test
    @DisplayName("validateToken 对空白字符串返回 false")
    void shouldRejectBlankToken() {
        assertFalse(jwtService.validateToken("   "));
    }

    @Test
    @DisplayName("validateToken 对 null 返回 false")
    void shouldRejectNullToken() {
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    @DisplayName("过期 Token 解析时抛出 ExpiredJwtException")
    void shouldThrowForExpiredToken() throws InterruptedException {
        JwtProperties shortProps = new JwtProperties();
        shortProps.setSecret("test-secret-key-for-unit-test-2026-min-length-32");
        shortProps.setExpiration(1); // 1 second
        JwtService shortJwtService = new JwtService(shortProps);

        String token = shortJwtService.generateToken("admin", 1L);
        assertTrue(shortJwtService.validateToken(token));

        Thread.sleep(1500);

        assertFalse(shortJwtService.validateToken(token));
        assertThrows(ExpiredJwtException.class, () -> shortJwtService.parseToken(token));
    }

    @Test
    @DisplayName("不同密钥签名的 Token 验证失败")
    void shouldRejectTokenWithDifferentKey() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("other-secret-key-for-unit-test-2026-32chars");
        otherProps.setExpiration(3600);
        JwtService otherJwtService = new JwtService(otherProps);

        String token = jwtService.generateToken("admin", 1L);

        assertThrows(JwtException.class, () -> otherJwtService.parseToken(token));
    }

    @Test
    @DisplayName("getExpirationSeconds 返回配置的过期时间")
    void shouldReturnConfiguredExpiration() {
        assertEquals(2, jwtService.getExpirationSeconds());
    }
}
