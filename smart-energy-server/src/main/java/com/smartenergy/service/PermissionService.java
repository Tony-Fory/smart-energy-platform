package com.smartenergy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.entity.*;
import com.smartenergy.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限服务
 * <p>
 * 从数据库加载用户角色和权限，提供 hasPermission 判断。
 * 权限以数据库为准，不写入 JWT。
 *
 * @author smart-energy
 */
@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class PermissionService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 查询用户拥有的所有权限编码
     */
    public Set<String> getUserPermissions(Long userId) {
        // 1. 查询用户角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();

        // 2. 查询启用的角色
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getStatus, 1));
        if (roles.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> activeRoleIds = roles.stream().map(SysRole::getId).toList();

        // 3. 查询角色-权限关联
        List<SysRolePermission> rolePermissions = sysRolePermissionMapper.selectList(
                new LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, activeRoleIds));
        if (rolePermissions.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> permissionIds = rolePermissions.stream()
                .map(SysRolePermission::getPermissionId).distinct().toList();

        // 4. 查询启用的权限
        List<SysPermission> permissions = sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getId, permissionIds)
                        .eq(SysPermission::getStatus, 1));

        return permissions.stream()
                .map(SysPermission::getPermissionCode)
                .collect(Collectors.toSet());
    }

    /**
     * 查询用户角色编码
     */
    public String getUserRoleCode(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return null;
        }
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getStatus, 1));
        if (roles.isEmpty()) {
            return null;
        }
        // 返回第一个角色编码
        return roles.get(0).getRoleCode();
    }

    /**
     * 检查当前用户是否有指定权限
     */
    public boolean hasPermission(Long userId, String permissionCode) {
        Set<String> permissions = getUserPermissions(userId);
        return permissions.contains(permissionCode);
    }
}
