package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartenergy.common.PageResult;
import com.smartenergy.entity.AlarmRecord;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.AlarmRecordMapper;
import com.smartenergy.vo.AlarmRecordVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlarmRecordService 单元测试")
class AlarmRecordServiceImplTest {

    @Mock private AlarmRecordMapper alarmRecordMapper;
    @InjectMocks private AlarmRecordServiceImpl alarmRecordService;

    private AlarmRecord record;

    @BeforeEach
    void setUp() {
        record = new AlarmRecord();
        record.setId(1L);
        record.setDeviceId(1L);
        record.setDeviceCode("DEVICE001");
        record.setRuleId(1L);
        record.setRuleName("功率过高告警");
        record.setMetric("POWER");
        record.setActualValue(1200.0);
        record.setThreshold(1000.0);
        record.setSeverity("WARNING");
        record.setStatus(0); // 未处理
        record.setAlarmTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("分页查询告警记录")
    void shouldListRecords() {
        Page<AlarmRecord> page = new Page<>(1, 10);
        page.setRecords(List.of(record));
        page.setTotal(1);
        when(alarmRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<AlarmRecordVO> result = alarmRecordService.listRecords(1, 10, null, null, null);
        assertEquals(1, result.getTotal());
        assertEquals("DEVICE001", result.getRecords().get(0).getDeviceCode());
    }

    @Test
    @DisplayName("查询告警详情")
    void shouldGetRecord() {
        when(alarmRecordMapper.selectById(1L)).thenReturn(record);
        AlarmRecordVO vo = alarmRecordService.getRecord(1L);
        assertEquals("DEVICE001", vo.getDeviceCode());
        assertEquals(1200.0, vo.getActualValue());
    }

    @Test
    @DisplayName("查询不存在的告警抛出异常")
    void shouldThrowWhenRecordNotFound() {
        when(alarmRecordMapper.selectById(999L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> alarmRecordService.getRecord(999L));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("未处理告警确认成功")
    void shouldAckUnprocessedAlarm() {
        when(alarmRecordMapper.selectById(1L)).thenReturn(record);
        alarmRecordService.ackAlarm(1L);
        assertEquals(1, record.getStatus());
        verify(alarmRecordMapper).updateById(record);
    }

    @Test
    @DisplayName("已确认告警不能重复确认")
    void shouldRejectAckForAlreadyAcked() {
        record.setStatus(1); // 已确认
        when(alarmRecordMapper.selectById(1L)).thenReturn(record);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> alarmRecordService.ackAlarm(1L));
        assertTrue(ex.getMessage().contains("不允许确认"));
    }

    @Test
    @DisplayName("已恢复告警不能确认")
    void shouldRejectAckForRecovered() {
        record.setStatus(2); // 已恢复
        when(alarmRecordMapper.selectById(1L)).thenReturn(record);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> alarmRecordService.ackAlarm(1L));
        assertTrue(ex.getMessage().contains("不允许确认"));
    }

    @Test
    @DisplayName("告警不存在确认失败")
    void shouldThrowWhenAckNotFound() {
        when(alarmRecordMapper.selectById(999L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> alarmRecordService.ackAlarm(999L));
        assertEquals(404, ex.getCode());
    }
}
