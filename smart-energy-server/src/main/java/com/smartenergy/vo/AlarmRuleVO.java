package com.smartenergy.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmRuleVO {
    private Long id;
    private Long deviceId;
    private String ruleName;
    private String metric;
    private String operator;
    private Double threshold;
    private String severity;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
