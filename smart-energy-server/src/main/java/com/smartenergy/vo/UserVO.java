package com.smartenergy.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息返回对象
 *
 * @author smart-energy
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private Integer status;

    private LocalDateTime createTime;
}
