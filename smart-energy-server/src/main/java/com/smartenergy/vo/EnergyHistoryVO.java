package com.smartenergy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 能源历史数据返回对象
 *
 * @author smart-energy
 */
@Data
public class EnergyHistoryVO {

    private String deviceCode;

    private List<EnergyDataPoint> list;

    /**
     * 单条能源数据点
     */
    @Data
    public static class EnergyDataPoint {

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime collectTime;

        private Double voltage;

        private Double current;

        private Double power;

        private Double energy;
    }
}
