package com.smartenergy.security;

/**
 * 认证上下文
 * <p>
 * 使用 ThreadLocal 保存当前请求的用户信息，由 JwtAuthFilter 设置。
 * 不依赖 Spring Security，仅用于当前请求线程内传递用户身份。
 *
 * @author smart-energy
 */
public class AuthContext {

    private static final ThreadLocal<UserInfo> CONTEXT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(UserInfo userInfo) {
        CONTEXT.set(userInfo);
    }

    public static UserInfo get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 当前请求用户信息
     */
    public record UserInfo(Long userId, String username) {
    }
}
