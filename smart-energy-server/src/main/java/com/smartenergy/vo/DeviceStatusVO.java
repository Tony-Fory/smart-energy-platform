package com.smartenergy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备实时状态（Redis 缓存）
 *
 * @author smart-energy
 */
@Data
public class DeviceStatusVO {

    private String deviceCode;

    private Double voltage;

    private Double current;

    private Double power;

    private Double energy;

    private Boolean online;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
