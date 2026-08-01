package com.smartenergy.controller;

import com.smartenergy.common.Result;
import com.smartenergy.service.SysUserService;
import com.smartenergy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理接口
 *
 * @author smart-energy
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @GetMapping("/users")
    public Result<List<UserVO>> listUsers() {
        List<UserVO> users = sysUserService.listUsers();
        return Result.success(users);
    }
}
