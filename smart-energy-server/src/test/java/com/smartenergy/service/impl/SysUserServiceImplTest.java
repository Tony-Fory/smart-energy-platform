package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.entity.SysUser;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * SysUserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysUserService 单元测试")
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    private SysUser adminUser;

    @BeforeEach
    void setUp() {
        // Inject real BCryptPasswordEncoder into the service
        sysUserService = new SysUserServiceImpl(sysUserMapper, passwordEncoder);

        adminUser = new SysUser();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setNickname("管理员");
        adminUser.setStatus(1);
    }

    @Test
    @DisplayName("BCrypt 密码验证成功")
    void shouldVerifyCorrectPassword() {
        String rawPassword = "admin123";
        assertTrue(passwordEncoder.matches(rawPassword, adminUser.getPassword()));
    }

    @Test
    @DisplayName("BCrypt 密码验证失败")
    void shouldRejectWrongPassword() {
        assertFalse(passwordEncoder.matches("wrongpassword", adminUser.getPassword()));
    }

    @Test
    @DisplayName("登录成功返回用户实体")
    void shouldLoginSuccessfully() {
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(adminUser);

        SysUser user = sysUserService.login("admin", "admin123");

        assertNotNull(user);
        assertEquals("admin", user.getUsername());
        assertEquals(1L, user.getId());
    }

    @Test
    @DisplayName("用户不存在时登录失败")
    void shouldFailWhenUserNotFound() {
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.login("nobody", "password"));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("密码错误时登录失败")
    void shouldFailWhenPasswordWrong() {
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(adminUser);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.login("admin", "wrongpassword"));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("用户名或密码错误"));
    }

    @Test
    @DisplayName("用户被禁用时登录失败")
    void shouldFailWhenUserDisabled() {
        adminUser.setStatus(0);
        when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(adminUser);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.login("admin", "admin123"));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("已被禁用"));
    }
}
