package com.smartenergy.simulator.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备能源数据
 */
@Data
@Builder
public class EnergyData {

    private String deviceCode;

    private double voltage;

    private double current;

    private double power;

    private double energy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime collectTime;
}
