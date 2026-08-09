package com.smartenergy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alarm_record")
public class AlarmRecord {
    @TableId(type = IdType.AUTO)
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
