package com.smartenergy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.dto.EnergyDataDTO;
import com.smartenergy.entity.AlarmRecord;
import com.smartenergy.entity.AlarmRule;
import com.smartenergy.entity.Device;
import com.smartenergy.mapper.AlarmRecordMapper;
import com.smartenergy.mapper.AlarmRuleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警检测服务
 * <p>
 * 在能源数据上报流程中检测告警规则，自动生成或恢复告警记录。
 * 同一设备+同一规则的未恢复告警(status=0或1)最多一条，避免重复创建。
 *
 * @author smart-energy
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmDetectService {

    private final AlarmRuleMapper alarmRuleMapper;
    private final AlarmRecordMapper alarmRecordMapper;

    /**
     * 根据能源数据检测告警规则并处理告警/恢复。
     * 在事务中执行，保证查询-插入的原子性。
     */
    @Transactional
    public void detectAndProcess(EnergyDataDTO dto, Device device) {
        // 1. 查询所有启用的告警规则（全局 + 特定设备）
        List<AlarmRule> rules = alarmRuleMapper.selectList(
                new LambdaQueryWrapper<AlarmRule>()
                        .eq(AlarmRule::getStatus, 1)
                        .and(w -> w.isNull(AlarmRule::getDeviceId)
                                .or().eq(AlarmRule::getDeviceId, device.getId())));

        if (rules.isEmpty()) {
            return;
        }

        for (AlarmRule rule : rules) {
            double actual = getMetricValue(dto, rule.getMetric());
            boolean triggered = evaluateRule(actual, rule.getOperator(), rule.getThreshold());

            // 查询该设备+规则的未恢复告警记录（status=0 未处理 或 status=1 已确认）
            List<AlarmRecord> existingRecords = alarmRecordMapper.selectList(
                    new LambdaQueryWrapper<AlarmRecord>()
                            .eq(AlarmRecord::getDeviceId, device.getId())
                            .eq(AlarmRecord::getRuleId, rule.getId())
                            .in(AlarmRecord::getStatus, 0, 1) // 未恢复
            );

            boolean hasUnresolved = !existingRecords.isEmpty();

            if (triggered && !hasUnresolved) {
                // 触发告警
                createAlarm(dto, device, rule, actual);
            } else if (!triggered && hasUnresolved) {
                // 自动恢复
                recoverAlarms(existingRecords, actual);
            }
            // triggered && hasUnresolved：持续告警，不重复创建
            // !triggered && !hasUnresolved：正常状态，不处理
        }
    }

    private void createAlarm(EnergyDataDTO dto, Device device, AlarmRule rule, double actual) {
        AlarmRecord record = new AlarmRecord();
        record.setDeviceId(device.getId());
        record.setDeviceCode(device.getDeviceCode());
        record.setRuleId(rule.getId());
        record.setRuleName(rule.getRuleName());
        record.setMetric(rule.getMetric());
        record.setActualValue(actual);
        record.setThreshold(rule.getThreshold());
        record.setSeverity(rule.getSeverity());
        record.setStatus(0); // 未处理
        record.setAlarmTime(dto.getCollectTime() != null ? dto.getCollectTime() : LocalDateTime.now());
        alarmRecordMapper.insert(record);
        log.info("告警触发: device={}, rule={}, metric={}, actual={}, threshold={}",
                device.getDeviceCode(), rule.getRuleName(), rule.getMetric(), actual, rule.getThreshold());
    }

    private void recoverAlarms(List<AlarmRecord> records, double actual) {
        LocalDateTime now = LocalDateTime.now();
        for (AlarmRecord record : records) {
            record.setStatus(2); // 已恢复
            record.setRecoverTime(now);
            record.setActualValue(actual);
            alarmRecordMapper.updateById(record);
            log.info("告警恢复: device={}, rule={}, metric={}, actual={}",
                    record.getDeviceCode(), record.getRuleName(), record.getMetric(), actual);
        }
    }

    /**
     * 根据 metric 从 DTO 中提取实际值
     */
    static double getMetricValue(EnergyDataDTO dto, String metric) {
        return switch (metric.toUpperCase()) {
            case "POWER" -> dto.getPower();
            case "VOLTAGE" -> dto.getVoltage();
            case "CURRENT" -> dto.getCurrent();
            case "ENERGY" -> dto.getEnergy();
            default -> throw new IllegalArgumentException("未知指标: " + metric);
        };
    }

    /**
     * 根据运算符比较实际值和阈值
     */
    static boolean evaluateRule(double actual, String operator, double threshold) {
        return switch (operator.toUpperCase()) {
            case "GT" -> actual > threshold;
            case "GTE" -> actual >= threshold;
            case "LT" -> actual < threshold;
            case "LTE" -> actual <= threshold;
            default -> throw new IllegalArgumentException("未知运算符: " + operator);
        };
    }
}
