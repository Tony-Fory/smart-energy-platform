package com.smartenergy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dashboard 相关 VO
 *
 * @author smart-energy
 */
public class DashboardVO {

    /**
     * 概览统计
     */
    @Data
    public static class Overview {
        private int deviceCount;
        private int onlineCount;
        private double totalPower;
        private double todayEnergy;
    }

    /**
     * 功率趋势数据点
     */
    @Data
    public static class PowerPoint {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime collectTime;
        private double totalPower;
    }

    /**
     * 功率趋势返回
     */
    @Data
    public static class PowerTrend {
        private List<PowerPoint> list;
    }
}
