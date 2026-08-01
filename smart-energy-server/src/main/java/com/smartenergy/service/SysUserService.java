package com.smartenergy.service;

import com.smartenergy.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author smart-energy
 */
public interface SysUserService {

    /**
     * 查询用户列表
     *
     * @return 用户列表
     */
    List<UserVO> listUsers();
}
