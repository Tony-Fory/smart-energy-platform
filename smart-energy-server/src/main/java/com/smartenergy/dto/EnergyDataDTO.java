package com.smartenergy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 设备能源数据上报请求
 *
 * @author smart-energy
 */
@Data
public class EnergyDataDTO {

    @NotBlank(message = "设备编号不能为空")
    private String deviceCode;

    @NotNull(message = "电压不能为空")
    private Double voltage;

    @NotNull(message = "电流不能为空")
    private Double current;

    @NotNull(message = "功率不能为空")
    private Double power;

    @NotNull(message = "累计电量不能为空")
    private Double energy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime collectTime;
}
