package com.smartenergy.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备信息返回对象
 *
 * @author smart-energy
 */
@Data
public class DeviceVO {

    private Long id;

    private String deviceCode;

    private String deviceName;

    private String deviceType;

    private String location;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
