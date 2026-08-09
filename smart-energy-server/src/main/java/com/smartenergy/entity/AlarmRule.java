package com.smartenergy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alarm_rule")
public class AlarmRule {
    @TableId(type = IdType.AUTO)
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
