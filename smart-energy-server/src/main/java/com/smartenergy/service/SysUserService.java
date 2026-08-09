package com.smartenergy.service;

import com.smartenergy.entity.SysUser;
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
     */
    List<UserVO> listUsers();

    /**
     * 用户登录认证
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 用户实体（认证通过）
     * @throws com.smartenergy.exception.BusinessException 用户名不存在或密码错误
     */
    SysUser login(String username, String password);
}
