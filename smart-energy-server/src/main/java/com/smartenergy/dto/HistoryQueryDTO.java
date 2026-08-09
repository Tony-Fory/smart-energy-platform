package com.smartenergy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class HistoryQueryDTO {

    public static final Set<String> VALID_METRICS = Set.of("POWER", "VOLTAGE", "CURRENT", "ENERGY");

    /** interval → TDengine INTERVAL 子句映射 */
    public static final java.util.Map<String, String> INTERVAL_SQL = java.util.Map.of(
            "RAW", "",
            "1m", "1m",
            "5m", "5m",
            "15m", "15m",
            "1h", "1h",
            "1d", "1d"
    );

    /** RAW 模式最大查询天数 */
    public static final long MAX_RAW_DAYS = 7;
    /** 聚合模式最大查询天数 */
    public static final long MAX_AGG_DAYS = 90;
    /** 最大返回条数 */
    public static final int MAX_LIMIT = 1000;

    @NotBlank(message = "设备编号不能为空")
    private String deviceCode;

    @NotBlank(message = "指标不能为空")
    private String metric;

    @NotNull(message = "开始时间不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    @NotBlank(message = "聚合粒度不能为空")
    private String interval = "RAW";

    @Min(1)
    @Max(MAX_LIMIT)
    private int limit = 100;
}
