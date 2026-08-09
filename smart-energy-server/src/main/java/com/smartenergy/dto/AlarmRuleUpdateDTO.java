package com.smartenergy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlarmRuleUpdateDTO {

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
