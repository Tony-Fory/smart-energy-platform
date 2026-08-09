package com.smartenergy.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应
 *
 * @author smart-energy
 */
@Data
@Builder
public class LoginVO {

    private String token;

    private String tokenType;

    private long expiresIn;
}
