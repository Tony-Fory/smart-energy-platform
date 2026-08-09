package com.smartenergy.controller;

import com.smartenergy.common.Result;
import com.smartenergy.dto.LoginDTO;
import com.smartenergy.security.JwtService;
import com.smartenergy.service.SysUserService;
import com.smartenergy.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 *
 * @author smart-energy
 */
@Tag(name = "认证", description = "用户登录认证接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtService jwtService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        var user = sysUserService.login(dto.getUsername(), dto.getPassword());

        String token = jwtService.generateToken(user.getUsername(), user.getId());

        LoginVO vo = LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationSeconds())
                .build();

        return Result.success(vo);
    }
}
