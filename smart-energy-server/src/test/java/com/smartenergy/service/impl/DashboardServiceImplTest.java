package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.smartenergy.mapper.DeviceMapper;
import com.smartenergy.service.RedisService;
import com.smartenergy.vo.DashboardVO;
import com.smartenergy.vo.DeviceStatusVO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * DashboardService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService 单元测试")
class DashboardServiceImplTest {

    @Mock
    private DeviceMapper deviceMapper;

    @Mock
    private RedisService redisService;

    @Mock
    private JdbcTemplate tdengineJdbcTemplate;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private List<DeviceStatusVO> mockStatuses;

    @BeforeEach
    void setUp() {
        DeviceStatusVO s1 = new DeviceStatusVO();
        s1.setDeviceCode("DEVICE001");
        s1.setPower(1100.0);
        s1.setEnergy(100.5);
        s1.setOnline(true);
        s1.setUpdateTime(LocalDateTime.now());

        DeviceStatusVO s2 = new DeviceStatusVO();
        s2.setDeviceCode("DEVICE002");
        s2.setPower(350.0);
        s2.setEnergy(50.2);
        s2.setOnline(true);
        s2.setUpdateTime(LocalDateTime.now());

        mockStatuses = new ArrayList<>();
        mockStatuses.add(s1);
        mockStatuses.add(s2);
    }

    @Test
    @DisplayName("概览统计 - 设备存在且有在线设备")
    void shouldReturnOverviewWithOnlineDevices() {
        when(deviceMapper.selectCount(isNull())).thenReturn(5L);
        when(redisService.getAllDeviceStatuses()).thenReturn(mockStatuses);

        DashboardVO.Overview result = dashboardService.getOverview();

        assertEquals(5, result.getDeviceCount());
        assertEquals(2, result.getOnlineCount());
        assertEquals(1450.0, result.getTotalPower());
        assertEquals(150.7, result.getTodayEnergy());
    }

    @Test
    @DisplayName("概览统计 - 无在线设备时在线数和功率为零")
    void shouldReturnZeroWhenNoOnlineDevices() {
        when(deviceMapper.selectCount(isNull())).thenReturn(3L);
        when(redisService.getAllDeviceStatuses()).thenReturn(new ArrayList<>());

        DashboardVO.Overview result = dashboardService.getOverview();

        assertEquals(3, result.getDeviceCount());
        assertEquals(0, result.getOnlineCount());
        assertEquals(0.0, result.getTotalPower());
        assertEquals(0.0, result.getTodayEnergy());
    }

    @Test
    @DisplayName("设备状态列表 - 返回 Redis 中的在线设备")
    void shouldReturnDeviceStatusList() {
        when(redisService.getAllDeviceStatuses()).thenReturn(mockStatuses);

        List<DeviceStatusVO> result = dashboardService.getDeviceStatusList();

        assertEquals(2, result.size());
        assertEquals("DEVICE001", result.get(0).getDeviceCode());
        assertTrue(result.get(0).getOnline());
    }

    @Test
    @DisplayName("功率趋势 - 返回最近1小时数据")
    @SuppressWarnings("unchecked")
    void shouldReturnPowerTrend() {
        List<DashboardVO.PowerPoint> mockPoints = new ArrayList<>();
        DashboardVO.PowerPoint p = new DashboardVO.PowerPoint();
        p.setCollectTime(LocalDateTime.now().minusMinutes(30));
        p.setTotalPower(1100.0);
        mockPoints.add(p);

        when(tdengineJdbcTemplate.query(
                anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(mockPoints);

        DashboardVO.PowerTrend result = dashboardService.getPowerTrend();

        assertNotNull(result);
        assertNotNull(result.getList());
        assertEquals(1, result.getList().size());
        assertEquals(1100.0, result.getList().get(0).getTotalPower());
    }

    @Test
    @DisplayName("功率趋势 - TDengine 无数据时返回空列表")
    @SuppressWarnings("unchecked")
    void shouldReturnEmptyPowerTrendWhenNoData() {
        when(tdengineJdbcTemplate.query(
                anyString(), any(Object[].class), any(RowMapper.class)))
                .thenReturn(new ArrayList<>());

        DashboardVO.PowerTrend result = dashboardService.getPowerTrend();

        assertNotNull(result);
        assertTrue(result.getList().isEmpty());
    }
}
