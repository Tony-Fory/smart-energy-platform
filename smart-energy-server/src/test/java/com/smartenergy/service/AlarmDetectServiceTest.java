package com.smartenergy.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.dto.EnergyDataDTO;
import com.smartenergy.entity.AlarmRecord;
import com.smartenergy.entity.AlarmRule;
import com.smartenergy.entity.Device;
import com.smartenergy.mapper.AlarmRecordMapper;
import com.smartenergy.mapper.AlarmRuleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlarmDetectService 单元测试")
class AlarmDetectServiceTest {

    @Mock private AlarmRuleMapper alarmRuleMapper;
    @Mock private AlarmRecordMapper alarmRecordMapper;
    @InjectMocks private AlarmDetectService alarmDetectService;

    private Device device;
    private EnergyDataDTO dto;
    private AlarmRule powerRule;

    @BeforeEach
    void setUp() {
        device = new Device();
        device.setId(1L);
        device.setDeviceCode("DEVICE001");
        device.setDeviceType("TV");

        dto = new EnergyDataDTO();
        dto.setDeviceCode("DEVICE001");
        dto.setVoltage(220.0);
        dto.setCurrent(5.0);
        dto.setPower(1100.0);
        dto.setEnergy(150.0);

        powerRule = new AlarmRule();
        powerRule.setId(1L);
        powerRule.setDeviceId(1L);
        powerRule.setRuleName("功率过高告警");
        powerRule.setMetric("POWER");
        powerRule.setOperator("GT");
        powerRule.setThreshold(1000.0);
        powerRule.setSeverity("WARNING");
        powerRule.setStatus(1);
    }

    // ==================== getMetricValue ====================

    @Test
    @DisplayName("getMetricValue 正确提取各指标值")
    void shouldExtractMetricValues() {
        assertEquals(1100.0, AlarmDetectService.getMetricValue(dto, "POWER"));
        assertEquals(220.0, AlarmDetectService.getMetricValue(dto, "VOLTAGE"));
        assertEquals(5.0, AlarmDetectService.getMetricValue(dto, "CURRENT"));
        assertEquals(150.0, AlarmDetectService.getMetricValue(dto, "ENERGY"));
    }

    // ==================== evaluateRule ====================

    @Test
    @DisplayName("GT: 超过阈值返回 true")
    void gtShouldTriggerWhenExceeded() {
        assertTrue(AlarmDetectService.evaluateRule(1100.0, "GT", 1000.0));
        assertFalse(AlarmDetectService.evaluateRule(1000.0, "GT", 1000.0));
    }

    @Test
    @DisplayName("GTE: 等于阈值返回 true")
    void gteShouldTriggerWhenEqual() {
        assertTrue(AlarmDetectService.evaluateRule(1000.0, "GTE", 1000.0));
        assertFalse(AlarmDetectService.evaluateRule(999.0, "GTE", 1000.0));
    }

    @Test
    @DisplayName("LT: 低于阈值返回 true")
    void ltShouldTriggerWhenBelow() {
        assertTrue(AlarmDetectService.evaluateRule(100.0, "LT", 200.0));
        assertFalse(AlarmDetectService.evaluateRule(200.0, "LT", 200.0));
    }

    @Test
    @DisplayName("LTE: 等于阈值返回 true")
    void lteShouldTriggerWhenEqual() {
        assertTrue(AlarmDetectService.evaluateRule(200.0, "LTE", 200.0));
        assertFalse(AlarmDetectService.evaluateRule(201.0, "LTE", 200.0));
    }

    // ==================== detectAndProcess ====================

    @Test
    @DisplayName("超过阈值且无未恢复告警 → 触发告警")
    void shouldCreateAlarmWhenTriggered() {
        when(alarmRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(powerRule));
        when(alarmRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        alarmDetectService.detectAndProcess(dto, device);

        ArgumentCaptor<AlarmRecord> captor = ArgumentCaptor.forClass(AlarmRecord.class);
        verify(alarmRecordMapper).insert(captor.capture());
        AlarmRecord record = captor.getValue();
        assertEquals(1L, record.getDeviceId());
        assertEquals("DEVICE001", record.getDeviceCode());
        assertEquals(1L, record.getRuleId());
        assertEquals(1100.0, record.getActualValue());
        assertEquals(0, record.getStatus());
    }

    @Test
    @DisplayName("未超过阈值 → 不触发告警")
    void shouldNotTriggerWhenBelowThreshold() {
        dto.setPower(500.0);
        when(alarmRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(powerRule));
        when(alarmRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        alarmDetectService.detectAndProcess(dto, device);

        verify(alarmRecordMapper, never()).insert(any(AlarmRecord.class));
    }

    @Test
    @DisplayName("已存在未恢复告警(状态0)时不重复创建")
    void shouldNotDuplicateUnresolvedAlarm() {
        AlarmRecord existing = new AlarmRecord();
        existing.setId(10L);
        existing.setDeviceId(1L);
        existing.setRuleId(1L);
        existing.setStatus(0);

        when(alarmRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(powerRule));
        when(alarmRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existing));

        alarmDetectService.detectAndProcess(dto, device);

        verify(alarmRecordMapper, never()).insert(any(AlarmRecord.class));
    }

    @Test
    @DisplayName("已存在已确认告警(状态1)时不重复创建")
    void shouldNotDuplicateAckedAlarm() {
        AlarmRecord existing = new AlarmRecord();
        existing.setId(10L);
        existing.setDeviceId(1L);
        existing.setRuleId(1L);
        existing.setStatus(1);

        when(alarmRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(powerRule));
        when(alarmRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existing));

        alarmDetectService.detectAndProcess(dto, device);

        verify(alarmRecordMapper, never()).insert(any(AlarmRecord.class));
    }

    @Test
    @DisplayName("低于阈值但存在未恢复告警 → 自动恢复")
    void shouldRecoverWhenConditionClears() {
        dto.setPower(500.0);
        AlarmRecord existing = new AlarmRecord();
        existing.setId(10L);
        existing.setDeviceId(1L);
        existing.setRuleId(1L);
        existing.setStatus(0);

        when(alarmRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(powerRule));
        when(alarmRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existing));

        alarmDetectService.detectAndProcess(dto, device);

        verify(alarmRecordMapper).updateById(existing);
        assertEquals(2, existing.getStatus());
        assertNotNull(existing.getRecoverTime());
    }

    @Test
    @DisplayName("无有效规则时不做任何操作")
    void shouldDoNothingWhenNoRules() {
        when(alarmRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        alarmDetectService.detectAndProcess(dto, device);

        verify(alarmRecordMapper, never()).insert(any(AlarmRecord.class));
        verify(alarmRecordMapper, never()).updateById(any(AlarmRecord.class));
    }

    @Test
    @DisplayName("持续告警状态不重复创建也不恢复")
    void shouldNotChangeWhenPersistentlyTriggered() {
        AlarmRecord existing = new AlarmRecord();
        existing.setId(10L);
        existing.setDeviceId(1L);
        existing.setRuleId(1L);
        existing.setStatus(0);

        when(alarmRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(powerRule));
        when(alarmRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(existing));

        alarmDetectService.detectAndProcess(dto, device);

        verify(alarmRecordMapper, never()).insert(any(AlarmRecord.class));
        verify(alarmRecordMapper, never()).updateById(any(AlarmRecord.class));
    }

    @Test
    @DisplayName("全局规则(device_id=NULL)对任意设备生效")
    void shouldMatchGlobalRule() {
        powerRule.setDeviceId(null);
        when(alarmRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(powerRule));
        when(alarmRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        alarmDetectService.detectAndProcess(dto, device);

        verify(alarmRecordMapper).insert(any(AlarmRecord.class));
    }

    @Test
    @DisplayName("VOLTAGE 指标告警正常检测")
    void shouldDetectVoltageAlarm() {
        AlarmRule voltageRule = new AlarmRule();
        voltageRule.setId(2L);
        voltageRule.setRuleName("电压异常");
        voltageRule.setMetric("VOLTAGE");
        voltageRule.setOperator("LT");
        voltageRule.setThreshold(200.0);
        voltageRule.setSeverity("CRITICAL");
        voltageRule.setStatus(1);

        dto.setVoltage(190.0);
        when(alarmRuleMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(voltageRule));
        when(alarmRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        alarmDetectService.detectAndProcess(dto, device);

        ArgumentCaptor<AlarmRecord> captor = ArgumentCaptor.forClass(AlarmRecord.class);
        verify(alarmRecordMapper).insert(captor.capture());
        assertEquals("VOLTAGE", captor.getValue().getMetric());
        assertEquals(190.0, captor.getValue().getActualValue());
    }
}
