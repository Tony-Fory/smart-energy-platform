package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.dto.EnergyDataDTO;
import com.smartenergy.entity.Device;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.DeviceMapper;
import com.smartenergy.service.AlarmDetectService;
import com.smartenergy.service.RedisService;
import com.smartenergy.vo.DeviceStatusVO;
import com.smartenergy.vo.EnergyHistoryVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EnergyDataService 单元测试
 * <p>
 * 使用 Mockito 进行纯单元测试，不依赖 Spring 上下文和外部数据库。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EnergyDataService 单元测试")
class EnergyDataServiceImplTest {

    @Mock
    private DeviceMapper deviceMapper;

    @Mock
    private JdbcTemplate tdengineJdbcTemplate;

    @Mock
    private RedisService redisService;

    @Mock
    private AlarmDetectService alarmDetectService;

    @InjectMocks
    private EnergyDataServiceImpl energyDataService;

    private EnergyDataDTO validDto;
    private Device mockDevice;

    @BeforeEach
    void setUp() {
        validDto = new EnergyDataDTO();
        validDto.setDeviceCode("DEVICE001");
        validDto.setVoltage(220.5);
        validDto.setCurrent(5.0);
        validDto.setPower(1102.5);
        validDto.setEnergy(100.5);
        validDto.setCollectTime(LocalDateTime.of(2026, 8, 1, 10, 0, 0));

        mockDevice = new Device();
        mockDevice.setDeviceCode("DEVICE001");
        mockDevice.setDeviceType("TV");
    }

    @Test
    @DisplayName("正常写入 TDengine 并更新 Redis - 设备存在时")
    void shouldInsertIntoWhenDeviceExists() {
        when(deviceMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(mockDevice);
        when(tdengineJdbcTemplate.update(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        energyDataService.save(validDto);

        // 验证 TDengine 写入
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Timestamp> tsCaptor = ArgumentCaptor.forClass(Timestamp.class);
        verify(tdengineJdbcTemplate).update(
                sqlCaptor.capture(),
                tsCaptor.capture(),
                eq(220.5),
                eq(5.0),
                eq(1102.5),
                eq(100.5));

        String expectedSql = "INSERT INTO energy_data_DEVICE001 "
                + "USING energy_data TAGS ('DEVICE001', 'TV') "
                + "VALUES (?, ?, ?, ?, ?)";
        assertEquals(expectedSql, sqlCaptor.getValue());
        assertEquals(Timestamp.valueOf(validDto.getCollectTime()),
                tsCaptor.getValue());

        // 验证 Redis 写入
        ArgumentCaptor<DeviceStatusVO> statusCaptor = ArgumentCaptor.forClass(DeviceStatusVO.class);
        verify(redisService).saveDeviceStatus(statusCaptor.capture());
        DeviceStatusVO savedStatus = statusCaptor.getValue();
        assertEquals("DEVICE001", savedStatus.getDeviceCode());
        assertEquals(220.5, savedStatus.getVoltage());
        assertEquals(5.0, savedStatus.getCurrent());
        assertEquals(1102.5, savedStatus.getPower());
        assertEquals(100.5, savedStatus.getEnergy());
        assertTrue(savedStatus.getOnline());
        assertEquals(validDto.getCollectTime(), savedStatus.getUpdateTime());
    }

    @Test
    @DisplayName("设备不存在时抛出 BusinessException")
    void shouldThrowExceptionWhenDeviceNotFound() {
        when(deviceMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> energyDataService.save(validDto));

        assertEquals("设备不存在: DEVICE001", exception.getMessage());
        verify(tdengineJdbcTemplate, never())
                .update(anyString(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("collectTime 为 null 时使用当前时间")
    void shouldUseCurrentTimeWhenCollectTimeIsNull() {
        validDto.setCollectTime(null);
        when(deviceMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(mockDevice);
        when(tdengineJdbcTemplate.update(anyString(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        energyDataService.save(validDto);

        ArgumentCaptor<Timestamp> tsCaptor = ArgumentCaptor.forClass(Timestamp.class);
        verify(tdengineJdbcTemplate).update(
                anyString(),
                tsCaptor.capture(),
                any(), any(), any(), any());

        long diff = Math.abs(System.currentTimeMillis()
                - tsCaptor.getValue().getTime());
        assertTrue(diff < 2000,
                "Timestamp should be close to current time, but diff was " + diff + "ms");
    }

    @Test
    @DisplayName("查询历史数据 - 返回设备历史能源数据")
    @SuppressWarnings("unchecked")
    void shouldQueryHistorySuccessfully() {
        EnergyHistoryVO.EnergyDataPoint point = new EnergyHistoryVO.EnergyDataPoint();
        point.setCollectTime(LocalDateTime.of(2026, 8, 1, 10, 0, 0));
        point.setVoltage(220.5);
        point.setCurrent(5.0);
        point.setPower(1102.5);
        point.setEnergy(100.5);
        List<EnergyHistoryVO.EnergyDataPoint> mockList = new ArrayList<>();
        mockList.add(point);

        when(tdengineJdbcTemplate.query(
                anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(mockList);

        EnergyHistoryVO result = energyDataService.queryHistory("DEVICE001", 24, 100);

        assertEquals("DEVICE001", result.getDeviceCode());
        assertEquals(1, result.getList().size());
        assertEquals(220.5, result.getList().get(0).getVoltage());
        assertEquals(5.0, result.getList().get(0).getCurrent());
        assertEquals(1102.5, result.getList().get(0).getPower());
        assertEquals(100.5, result.getList().get(0).getEnergy());
    }

    @Test
    @DisplayName("查询历史数据 - 结果为空时返回空列表")
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyListWhenNoData() {
        when(tdengineJdbcTemplate.query(
                anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(new ArrayList<>());

        EnergyHistoryVO result = energyDataService.queryHistory("DEVICE001", 1, 50);

        assertEquals("DEVICE001", result.getDeviceCode());
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @DisplayName("查询实时状态 - Redis 有缓存时返回在线状态")
    void shouldReturnOnlineStatusWhenRedisHasData() {
        DeviceStatusVO cached = new DeviceStatusVO();
        cached.setDeviceCode("DEVICE001");
        cached.setVoltage(220.5);
        cached.setCurrent(5.0);
        cached.setPower(1102.5);
        cached.setEnergy(100.5);
        cached.setOnline(true);
        cached.setUpdateTime(LocalDateTime.of(2026, 8, 1, 10, 0, 0));

        when(redisService.getDeviceStatus("DEVICE001")).thenReturn(cached);

        DeviceStatusVO result = energyDataService.queryStatus("DEVICE001");

        assertNotNull(result);
        assertEquals("DEVICE001", result.getDeviceCode());
        assertEquals(220.5, result.getVoltage());
        assertTrue(result.getOnline());
    }

    @Test
    @DisplayName("查询实时状态 - Redis 无缓存时返回离线状态")
    void shouldReturnOfflineStatusWhenRedisMisses() {
        when(redisService.getDeviceStatus("DEVICE001")).thenReturn(null);

        DeviceStatusVO result = energyDataService.queryStatus("DEVICE001");

        assertNotNull(result);
        assertEquals("DEVICE001", result.getDeviceCode());
        assertFalse(result.getOnline());
        assertNull(result.getVoltage());
    }
}
