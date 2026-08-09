package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.entity.SysUser;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.SysUserMapper;
import com.smartenergy.service.SysUserService;
import com.smartenergy.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现
 *
 * @author smart-energy
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserVO> listUsers() {
        List<SysUser> users = sysUserMapper.selectList(null);
        return users.stream().map(this::toVO).toList();
    }

    @Override
    public SysUser login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (user == null) {
            throw BusinessException.badRequest("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw BusinessException.badRequest("用户已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }
        return user;
    }

    /**
     * Entity → VO 转换
     */
    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
