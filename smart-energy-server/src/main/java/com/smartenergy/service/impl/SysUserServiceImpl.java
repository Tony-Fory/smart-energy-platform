package com.smartenergy.service.impl;

import com.smartenergy.entity.SysUser;
import com.smartenergy.mapper.SysUserMapper;
import com.smartenergy.service.SysUserService;
import com.smartenergy.vo.UserVO;
import lombok.RequiredArgsConstructor;
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

    @Override
    public List<UserVO> listUsers() {
        List<SysUser> users = sysUserMapper.selectList(null);
        return users.stream().map(this::toVO).toList();
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
