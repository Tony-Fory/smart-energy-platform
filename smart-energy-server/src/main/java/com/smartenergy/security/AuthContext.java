package com.smartenergy.security;

import java.util.Collections;
import java.util.Set;

/**
 * 认证上下文
 * <p>
 * 使用 ThreadLocal 保存当前请求的用户信息，由 JwtAuthFilter 设置。
 * 权限信息由 JwtAuthFilter 在认证通过后从数据库加载。
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
    public static class UserInfo {
        private final Long userId;
        private final String username;
        private final String roleCode;
        private final Set<String> permissions;

        public UserInfo(Long userId, String username, String roleCode, Set<String> permissions) {
            this.userId = userId;
            this.username = username;
            this.roleCode = roleCode;
            this.permissions = permissions != null ? Collections.unmodifiableSet(permissions) : Collections.emptySet();
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRoleCode() { return roleCode; }
        public Set<String> getPermissions() { return permissions; }

        public boolean hasPermission(String code) {
            return permissions.contains(code);
        }
    }
}
