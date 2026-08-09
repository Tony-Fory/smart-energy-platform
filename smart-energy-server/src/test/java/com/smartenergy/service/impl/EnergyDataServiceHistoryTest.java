package com.smartenergy.service.impl;

import com.smartenergy.dto.HistoryQueryDTO;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.DeviceMapper;
import com.smartenergy.service.AlarmDetectService;
import com.smartenergy.service.RedisService;
import com.smartenergy.vo.HistoryDataVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnergyDataService 历史查询测试")
class EnergyDataServiceHistoryTest {

    @Mock private DeviceMapper deviceMapper;
    @Mock private JdbcTemplate tdengineJdbcTemplate;
    @Mock private RedisService redisService;
    @Mock private AlarmDetectService alarmDetectService;
    @InjectMocks private EnergyDataServiceImpl service;

    private HistoryQueryDTO validDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        validDto = new HistoryQueryDTO();
        validDto.setDeviceCode("DEVICE001");
        validDto.setMetric("POWER");
        validDto.setStartTime(now.minusHours(1));
        validDto.setEndTime(now);
        validDto.setInterval("RAW");
        validDto.setLimit(100);
    }

    // ==================== Parameter Validation ====================

    @Test
    @DisplayName("非法 metric 抛出异常")
    void shouldRejectInvalidMetric() {
        validDto.setMetric("INVALID");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.queryTimeSeries(validDto));
        assertTrue(ex.getMessage().contains("非法的指标"));
    }

    @Test
    @DisplayName("非法 interval 抛出异常")
    void shouldRejectInvalidInterval() {
        validDto.setInterval("10m");
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.queryTimeSeries(validDto));
        assertTrue(ex.getMessage().contains("非法的聚合粒度"));
    }

    @Test
    @DisplayName("开始时间为未来时间抛出异常")
    void shouldRejectFutureStartTime() {
        validDto.setStartTime(now.plusDays(1));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.queryTimeSeries(validDto));
        assertTrue(ex.getMessage().contains("未来时间"));
    }

    @Test
    @DisplayName("结束时间为未来时间抛出异常")
    void shouldRejectFutureEndTime() {
        validDto.setEndTime(now.plusDays(1));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.queryTimeSeries(validDto));
        assertTrue(ex.getMessage().contains("未来时间"));
    }

    @Test
    @DisplayName("startTime >= endTime 抛出异常")
    void shouldRejectInvalidTimeOrder() {
        validDto.setStartTime(now);
        validDto.setEndTime(now.minusHours(1));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.queryTimeSeries(validDto));
        assertTrue(ex.getMessage().contains("开始时间必须早于结束时间"));
    }

    @Test
    @DisplayName("RAW 模式超过 7 天抛出异常")
    void shouldRejectRawTooLong() {
        validDto.setStartTime(now.minusDays(8));
        validDto.setEndTime(now);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.queryTimeSeries(validDto));
        assertTrue(ex.getMessage().contains("RAW 模式最大查询范围"));
    }

    @Test
    @DisplayName("聚合模式超过 90 天抛出异常")
    void shouldRejectAggTooLong() {
        validDto.setInterval("1d");
        validDto.setStartTime(now.minusDays(92));
        validDto.setEndTime(now);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.queryTimeSeries(validDto));
        assertTrue(ex.getMessage().contains("聚合模式最大查询范围"));
    }

    // ==================== Metric whitelist ====================

    @Test
    @DisplayName("POWER 指标通过白名单")
    void shouldAcceptPowerMetric() throws Exception {
        validDto.setMetric("POWER");
        when(tdengineJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(new ArrayList<>());
        HistoryDataVO vo = service.queryTimeSeries(validDto);
        assertEquals("POWER", vo.getMetric());
    }

    @Test
    @DisplayName("VOLTAGE 指标通过白名单")
    void shouldAcceptVoltageMetric() throws Exception {
        validDto.setMetric("voltage"); // case insensitive
        when(tdengineJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(new ArrayList<>());
        assertEquals("VOLTAGE", service.queryTimeSeries(validDto).getMetric());
    }

    @Test
    @DisplayName("CURRENT 指标通过白名单")
    void shouldAcceptCurrentMetric() throws Exception {
        validDto.setMetric("CURRENT");
        when(tdengineJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(new ArrayList<>());
        assertEquals("CURRENT", service.queryTimeSeries(validDto).getMetric());
    }

    @Test
    @DisplayName("ENERGY 指标通过白名单")
    void shouldAcceptEnergyMetric() throws Exception {
        validDto.setMetric("ENERGY");
        when(tdengineJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(new ArrayList<>());
        assertEquals("ENERGY", service.queryTimeSeries(validDto).getMetric());
    }

    // ==================== Interval whitelist ====================

    @Test
    @DisplayName("RAW 模式查询成功")
    void shouldQueryRaw() throws Exception {
        when(tdengineJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(new ArrayList<>());
        HistoryDataVO vo = service.queryTimeSeries(validDto);
        assertEquals("RAW", vo.getInterval());
    }

    @Test
    @DisplayName("5m 聚合查询成功")
    void shouldQuery5mAgg() throws Exception {
        validDto.setInterval("5m");
        validDto.setStartTime(now.minusDays(1));
        when(tdengineJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(new ArrayList<>());
        HistoryDataVO vo = service.queryTimeSeries(validDto);
        assertEquals("5m", vo.getInterval());
    }

    // ==================== Service results ====================

    @Test
    @DisplayName("TDengine 返回 null 时返回空列表")
    void shouldReturnEmptyWhenNull() throws Exception {
        when(tdengineJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(null);
        HistoryDataVO vo = service.queryTimeSeries(validDto);
        assertNotNull(vo.getList());
        assertTrue(vo.getList().isEmpty());
    }
}
