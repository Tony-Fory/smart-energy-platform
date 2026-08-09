package com.smartenergy.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

/**
 * 当前用户信息（GET /api/auth/me）
 *
 * @author smart-energy
 */
@Data
@Builder
public class MeVO {
    private Long userId;
    private String username;
    private String roleCode;
    private Set<String> permissions;
}
