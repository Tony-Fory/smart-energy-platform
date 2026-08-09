package com.smartenergy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.entity.*;
import com.smartenergy.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * PermissionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService 单元测试")
class PermissionServiceTest {

    @Mock private SysUserRoleMapper sysUserRoleMapper;
    @Mock private SysRolePermissionMapper sysRolePermissionMapper;
    @Mock private SysRoleMapper sysRoleMapper;
    @Mock private SysPermissionMapper sysPermissionMapper;

    @InjectMocks
    private PermissionService permissionService;

    private SysRole adminRole, operatorRole, viewerRole;
    private SysPermission permDashboard, permDeviceView, permDeviceCreate, permDeviceUpdate, permDeviceDelete, permUserView;

    @BeforeEach
    void setUp() {
        adminRole = role(1L, "ADMIN", "管理员");
        operatorRole = role(2L, "OPERATOR", "操作员");
        viewerRole = role(3L, "VIEWER", "观察者");

        permDashboard = perm(1L, "DASHBOARD_VIEW");
        permDeviceView = perm(2L, "DEVICE_VIEW");
        permDeviceCreate = perm(3L, "DEVICE_CREATE");
        permDeviceUpdate = perm(4L, "DEVICE_UPDATE");
        permDeviceDelete = perm(5L, "DEVICE_DELETE");
        permUserView = perm(6L, "USER_VIEW");
    }

    private SysRole role(Long id, String code, String name) {
        SysRole r = new SysRole();
        r.setId(id); r.setRoleCode(code); r.setRoleName(name); r.setStatus(1);
        return r;
    }

    private SysPermission perm(Long id, String code) {
        SysPermission p = new SysPermission();
        p.setId(id); p.setPermissionCode(code); p.setPermissionName(code); p.setStatus(1);
        return p;
    }

    private void setupUserRole(Long userId, SysRole role) {
        SysUserRole ur = new SysUserRole();
        ur.setUserId(userId); ur.setRoleId(role.getId());
        when(sysUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(ur));
        when(sysRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(role));
    }

    private void setupRolePermissions(SysRole role, List<SysPermission> perms) {
        List<SysRolePermission> rps = perms.stream().map(p -> {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(role.getId()); rp.setPermissionId(p.getId());
            return rp;
        }).toList();
        when(sysRolePermissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(rps);
        when(sysPermissionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(perms);
    }

    @Test
    @DisplayName("ADMIN 拥有全部 6 项权限")
    void adminHasAllPermissions() {
        setupUserRole(1L, adminRole);
        setupRolePermissions(adminRole, List.of(permDashboard, permDeviceView, permDeviceCreate, permDeviceUpdate, permDeviceDelete, permUserView));

        assertTrue(permissionService.hasPermission(1L, "DASHBOARD_VIEW"));
        assertTrue(permissionService.hasPermission(1L, "DEVICE_VIEW"));
        assertTrue(permissionService.hasPermission(1L, "DEVICE_CREATE"));
        assertTrue(permissionService.hasPermission(1L, "DEVICE_UPDATE"));
        assertTrue(permissionService.hasPermission(1L, "DEVICE_DELETE"));
        assertTrue(permissionService.hasPermission(1L, "USER_VIEW"));
    }

    @Test
    @DisplayName("OPERATOR: DEVICE_VIEW/CREATE/UPDATE=true, DELETE=false")
    void operatorPermissions() {
        setupUserRole(2L, operatorRole);
        setupRolePermissions(operatorRole, List.of(permDashboard, permDeviceView, permDeviceCreate, permDeviceUpdate));

        assertTrue(permissionService.hasPermission(2L, "DASHBOARD_VIEW"));
        assertTrue(permissionService.hasPermission(2L, "DEVICE_VIEW"));
        assertTrue(permissionService.hasPermission(2L, "DEVICE_CREATE"));
        assertTrue(permissionService.hasPermission(2L, "DEVICE_UPDATE"));
        assertFalse(permissionService.hasPermission(2L, "DEVICE_DELETE"));
        assertFalse(permissionService.hasPermission(2L, "USER_VIEW"));
    }

    @Test
    @DisplayName("VIEWER: DASHBOARD_VIEW/DEVICE_VIEW=true, CREATE/UPDATE/DELETE=false")
    void viewerPermissions() {
        setupUserRole(3L, viewerRole);
        setupRolePermissions(viewerRole, List.of(permDashboard, permDeviceView));

        assertTrue(permissionService.hasPermission(3L, "DASHBOARD_VIEW"));
        assertTrue(permissionService.hasPermission(3L, "DEVICE_VIEW"));
        assertFalse(permissionService.hasPermission(3L, "DEVICE_CREATE"));
        assertFalse(permissionService.hasPermission(3L, "DEVICE_UPDATE"));
        assertFalse(permissionService.hasPermission(3L, "DEVICE_DELETE"));
        assertFalse(permissionService.hasPermission(3L, "USER_VIEW"));
    }

    @Test
    @DisplayName("无角色用户返回空权限")
    void noRoleUserHasNoPermissions() {
        when(sysUserRoleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        Set<String> perms = permissionService.getUserPermissions(99L);
        assertTrue(perms.isEmpty());
        assertFalse(permissionService.hasPermission(99L, "DASHBOARD_VIEW"));
    }

    @Test
    @DisplayName("ADMIN roleCode=ADMIN")
    void adminRoleCode() {
        setupUserRole(1L, adminRole);
        // getUserRoleCode only queries user_role + role, not permissions
        assertEquals("ADMIN", permissionService.getUserRoleCode(1L));
    }
}
