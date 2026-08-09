package com.smartenergy.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlarmRecordVO {
    private Long id;
    private Long deviceId;
    private String deviceCode;
    private Long ruleId;
    private String ruleName;
    private String metric;
    private Double actualValue;
    private Double threshold;
    private String severity;
    private Integer status;
    private LocalDateTime alarmTime;
    private LocalDateTime recoverTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
