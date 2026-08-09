package com.smartenergy.controller;

import com.smartenergy.common.Result;
import com.smartenergy.dto.LoginDTO;
import com.smartenergy.security.AuthContext;
import com.smartenergy.security.JwtService;
import com.smartenergy.service.PermissionService;
import com.smartenergy.service.SysUserService;
import com.smartenergy.vo.LoginVO;
import com.smartenergy.vo.MeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口
 *
 * @author smart-energy
 */
@Tag(name = "认证", description = "用户登录与当前用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtService jwtService;
    private final PermissionService permissionService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        var user = sysUserService.login(dto.getUsername(), dto.getPassword());

        String token = jwtService.generateToken(user.getUsername(), user.getId());
        String roleCode = permissionService.getUserRoleCode(user.getId());

        LoginVO vo = LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .roleCode(roleCode)
                .build();

        return Result.success(vo);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<MeVO> me() {
        AuthContext.UserInfo user = AuthContext.get();
        if (user == null) {
            return Result.success(null);
        }
        MeVO vo = MeVO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .roleCode(user.getRoleCode())
                .permissions(user.getPermissions())
                .build();
        return Result.success(vo);
    }
}
