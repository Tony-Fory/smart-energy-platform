package com.smartenergy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class AlarmRuleCreateDTO {

    /** 合法的监控指标 */
    public static final Set<String> VALID_METRICS = Set.of("POWER", "VOLTAGE", "CURRENT");
    /** 合法的运算符 */
    public static final Set<String> VALID_OPERATORS = Set.of("GT", "GTE", "LT", "LTE");
    /** 合法的严重级别 */
    public static final Set<String> VALID_SEVERITIES = Set.of("INFO", "WARNING", "CRITICAL");

    private Long deviceId;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "监控指标不能为空")
    private String metric;

    @NotBlank(message = "运算符不能为空")
    private String operator;

    @NotNull(message = "阈值不能为空")
    private Double threshold;

    @NotBlank(message = "严重级别不能为空")
    private String severity;

    private Integer status;
}
