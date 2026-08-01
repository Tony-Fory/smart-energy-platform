package com.smartenergy.service.impl;

import com.smartenergy.mapper.DeviceMapper;
import com.smartenergy.service.DashboardService;
import com.smartenergy.service.RedisService;
import com.smartenergy.vo.DashboardVO;
import com.smartenergy.vo.DeviceStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard 服务实现
 * <p>
 * 汇总 MySQL 设备数、Redis 在线状态、TDengine 历史数据，提供实时监控数据。
 *
 * @author smart-energy
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    private final DeviceMapper deviceMapper;
    private final RedisService redisService;
    private final JdbcTemplate tdengineJdbcTemplate;

    public DashboardServiceImpl(DeviceMapper deviceMapper,
                                RedisService redisService,
                                @Qualifier("tdengineJdbcTemplate") JdbcTemplate tdengineJdbcTemplate) {
        this.deviceMapper = deviceMapper;
        this.redisService = redisService;
        this.tdengineJdbcTemplate = tdengineJdbcTemplate;
    }

    @Override
    public DashboardVO.Overview getOverview() {
        // 设备总数（MySQL）
        long deviceCount = deviceMapper.selectCount(null);

        // 在线设备及功率（Redis）
        List<DeviceStatusVO> statuses = redisService.getAllDeviceStatuses();
        int onlineCount = statuses.size();
        double totalPower = statuses.stream()
                .mapToDouble(s -> s.getPower() != null ? s.getPower() : 0.0)
                .sum();
        double todayEnergy = statuses.stream()
                .mapToDouble(s -> s.getEnergy() != null ? s.getEnergy() : 0.0)
                .sum();

        DashboardVO.Overview overview = new DashboardVO.Overview();
        overview.setDeviceCount((int) deviceCount);
        overview.setOnlineCount(onlineCount);
        overview.setTotalPower(Math.round(totalPower * 10.0) / 10.0);
        overview.setTodayEnergy(Math.round(todayEnergy * 100.0) / 100.0);

        log.info("Dashboard 概览: devices={}, online={}, power={}W, energy={}kWh",
                deviceCount, onlineCount, overview.getTotalPower(), overview.getTodayEnergy());
        return overview;
    }

    @Override
    public List<DeviceStatusVO> getDeviceStatusList() {
        List<DeviceStatusVO> list = redisService.getAllDeviceStatuses();
        log.info("Dashboard 设备状态: {} 台在线", list.size());
        return list;
    }

    @Override
    public DashboardVO.PowerTrend getPowerTrend() {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        Timestamp sinceTs = Timestamp.valueOf(since);

        String sql = "SELECT ts, power FROM smart_energy.energy_data "
                + "WHERE ts >= ? "
                + "ORDER BY ts ASC "
                + "LIMIT 200";

        List<DashboardVO.PowerPoint> list = tdengineJdbcTemplate.query(
                sql,
                new Object[]{sinceTs},
                (rs, rowNum) -> {
                    DashboardVO.PowerPoint point = new DashboardVO.PowerPoint();
                    Timestamp dbTs = rs.getTimestamp("ts");
                    if (dbTs != null) {
                        point.setCollectTime(dbTs.toLocalDateTime());
                    }
                    point.setTotalPower(rs.getDouble("power"));
                    return point;
                });

        if (list == null) {
            list = new ArrayList<>();
        }

        DashboardVO.PowerTrend trend = new DashboardVO.PowerTrend();
        trend.setList(list);

        log.info("Dashboard 功率趋势: {} 个数据点", list.size());
        return trend;
    }
}
