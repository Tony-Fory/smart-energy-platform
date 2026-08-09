package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.dto.EnergyDataDTO;
import com.smartenergy.dto.HistoryQueryDTO;
import com.smartenergy.entity.Device;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.DeviceMapper;
import com.smartenergy.service.AlarmDetectService;
import com.smartenergy.service.EnergyDataService;
import com.smartenergy.service.RedisService;
import com.smartenergy.vo.DeviceStatusVO;
import com.smartenergy.vo.EnergyHistoryVO;
import com.smartenergy.vo.HistoryDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 能源数据服务实现
 * <p>
 * 接收模拟器上报的能源数据，写入 TDengine 时序数据库，同时更新 Redis 实时状态缓存。
 *
 * @author smart-energy
 */
@Slf4j
@Service
public class EnergyDataServiceImpl implements EnergyDataService {

    private final DeviceMapper deviceMapper;
    private final JdbcTemplate tdengineJdbcTemplate;
    private final RedisService redisService;
    private final AlarmDetectService alarmDetectService;

    private static final String INSERT_SQL =
            "INSERT INTO energy_data_%s USING energy_data TAGS ('%s', '%s') VALUES (?, ?, ?, ?, ?)";

    /** deviceCode / deviceType 安全字符集：字母、数字、下划线、连字符 */
    private static final Pattern SAFE_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    /**
     * 显式构造函数，通过 @Qualifier 指定 TDengine 的 JdbcTemplate。
     */
    public EnergyDataServiceImpl(DeviceMapper deviceMapper,
                                 @Qualifier("tdengineJdbcTemplate") JdbcTemplate tdengineJdbcTemplate,
                                 RedisService redisService,
                                 AlarmDetectService alarmDetectService) {
        this.deviceMapper = deviceMapper;
        this.tdengineJdbcTemplate = tdengineJdbcTemplate;
        this.redisService = redisService;
        this.alarmDetectService = alarmDetectService;
    }

    @Override
    public void save(EnergyDataDTO dto) {
        log.info("收到能源数据: deviceCode={}, voltage={}V, current={}A, power={}W, energy={}kWh, time={}",
                dto.getDeviceCode(),
                dto.getVoltage(),
                dto.getCurrent(),
                dto.getPower(),
                dto.getEnergy(),
                dto.getCollectTime());

        // 0. 防御性校验：deviceCode 必须为安全字符集，防止 TDengine 动态 SQL 注入
        if (!SAFE_CODE_PATTERN.matcher(dto.getDeviceCode()).matches()) {
            throw BusinessException.badRequest("设备编号包含非法字符: " + dto.getDeviceCode());
        }

        // 1. 从 MySQL 查询设备类型
        Device device = deviceMapper.selectOne(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getDeviceCode, dto.getDeviceCode()));
        if (device == null) {
            throw BusinessException.notFound("设备不存在: " + dto.getDeviceCode());
        }

        // 2. 防御性校验：deviceType 也必须为安全字符集
        if (!SAFE_CODE_PATTERN.matcher(device.getDeviceType()).matches()) {
            throw BusinessException.badRequest("设备类型包含非法字符: " + device.getDeviceType());
        }

        // 3. 构造 SQL 并写入 TDengine
        String sql = String.format(INSERT_SQL,
                dto.getDeviceCode(),
                dto.getDeviceCode(),
                device.getDeviceType());
        Timestamp ts = dto.getCollectTime() != null
                ? Timestamp.valueOf(dto.getCollectTime())
                : new Timestamp(System.currentTimeMillis());

        tdengineJdbcTemplate.update(sql,
                ts,
                dto.getVoltage(),
                dto.getCurrent(),
                dto.getPower(),
                dto.getEnergy());

        log.debug("写入 TDengine 成功: deviceCode={}", dto.getDeviceCode());

        // 4. 告警检测（在事务外，告警失败不影响数据写入）
        try {
            alarmDetectService.detectAndProcess(dto, device);
        } catch (Exception e) {
            log.error("告警检测异常: deviceCode={}", dto.getDeviceCode(), e);
        }

        // 5. 更新 Redis 实时状态缓存
        DeviceStatusVO status = new DeviceStatusVO();
        status.setDeviceCode(dto.getDeviceCode());
        status.setVoltage(dto.getVoltage());
        status.setCurrent(dto.getCurrent());
        status.setPower(dto.getPower());
        status.setEnergy(dto.getEnergy());
        status.setOnline(true);
        status.setUpdateTime(dto.getCollectTime() != null
                ? dto.getCollectTime()
                : LocalDateTime.now());
        redisService.saveDeviceStatus(status);
    }

    @Override
    public EnergyHistoryVO queryHistory(String deviceCode, int hours, int limit) {
        log.info("查询历史数据: deviceCode={}, hours={}, limit={}", deviceCode, hours, limit);

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        Timestamp sinceTs = Timestamp.valueOf(since);

        String tableName = "energy_data_" + deviceCode;
        String sql = "SELECT ts, voltage, current, power, energy "
                + "FROM " + tableName + " "
                + "WHERE ts >= ? "
                + "ORDER BY ts DESC "
                + "LIMIT ?";

        List<EnergyHistoryVO.EnergyDataPoint> list = tdengineJdbcTemplate.query(
                sql,
                new Object[]{sinceTs, limit},
                (rs, rowNum) -> {
                    EnergyHistoryVO.EnergyDataPoint point = new EnergyHistoryVO.EnergyDataPoint();
                    Timestamp dbTs = rs.getTimestamp("ts");
                    if (dbTs != null) {
                        point.setCollectTime(dbTs.toLocalDateTime());
                    }
                    point.setVoltage(rs.getDouble("voltage"));
                    point.setCurrent(rs.getDouble("current"));
                    point.setPower(rs.getDouble("power"));
                    point.setEnergy(rs.getDouble("energy"));
                    return point;
                });

        // 如果结果集为 null（TDengine JDBC 在某些版本可能返回 null），返回空列表
        if (list == null) {
            list = new ArrayList<>();
        }

        EnergyHistoryVO vo = new EnergyHistoryVO();
        vo.setDeviceCode(deviceCode);
        vo.setList(list);

        log.info("查询历史数据完成: deviceCode={}, 返回 {} 条记录", deviceCode, list.size());
        return vo;
    }

    /**
     * metric 白名单 → TDengine 实际列名
     */
    private static final Map<String, String> METRIC_COLUMN_MAP = Map.of(
            "POWER", "power",
            "VOLTAGE", "voltage",
            "CURRENT", "current",
            "ENERGY", "energy"
    );

    /**
     * 聚合函数：ENERGY 为累计值使用 MAX，其余使用 AVG
     */
    private static String aggFunc(String metric) {
        return "ENERGY".equalsIgnoreCase(metric) ? "MAX" : "AVG";
    }

    @Override
    public HistoryDataVO queryTimeSeries(HistoryQueryDTO dto) {
        // 0. 参数白名单校验
        String metricUpper = dto.getMetric().toUpperCase();
        if (!HistoryQueryDTO.VALID_METRICS.contains(metricUpper)) {
            throw BusinessException.badRequest("非法的指标: " + dto.getMetric());
        }
        String intervalKey = dto.getInterval(); // keep original case for map lookup
        if (!HistoryQueryDTO.INTERVAL_SQL.containsKey(intervalKey)) {
            throw BusinessException.badRequest("非法的聚合粒度: " + dto.getInterval());
        }

        // 1. 时间范围校验
        validateTimeRange(dto, intervalKey);

        String column = METRIC_COLUMN_MAP.get(metricUpper);
        String tableName = "energy_data_" + dto.getDeviceCode();
        Timestamp startTs = Timestamp.valueOf(dto.getStartTime());
        Timestamp endTs = Timestamp.valueOf(dto.getEndTime());

        String sql;
        Object[] params;

        if ("RAW".equals(intervalKey)) {
            sql = "SELECT ts, " + column + " FROM " + tableName
                    + " WHERE ts >= ? AND ts <= ? ORDER BY ts ASC LIMIT ?";
            params = new Object[]{startTs, endTs, dto.getLimit()};
        } else {
            String intervalClause = HistoryQueryDTO.INTERVAL_SQL.get(intervalKey);
            String func = aggFunc(metricUpper);
            sql = "SELECT _wstart as ts, " + func + "(" + column + ") as value FROM " + tableName
                    + " WHERE ts >= ? AND ts <= ?"
                    + " INTERVAL(" + intervalClause + ")"
                    + " LIMIT ?";
            params = new Object[]{startTs, endTs, dto.getLimit()};
        }

        log.info("历史数据查询: deviceCode={}, metric={}, interval={}", dto.getDeviceCode(), metricUpper, intervalKey);

        List<HistoryDataVO.DataPoint> list = tdengineJdbcTemplate.query(
                sql, params,
                (rs, rowNum) -> {
                    HistoryDataVO.DataPoint point = new HistoryDataVO.DataPoint();
                    Timestamp dbTs = rs.getTimestamp("ts");
                    if (dbTs != null) {
                        point.setTimestamp(dbTs.toLocalDateTime());
                    }
                    point.setValue(rs.getDouble("RAW".equals(intervalKey) ? column : "value"));
                    return point;
                });

        if (list == null) {
            list = new ArrayList<>();
        }

        HistoryDataVO vo = new HistoryDataVO();
        vo.setDeviceCode(dto.getDeviceCode());
        vo.setMetric(metricUpper);
        vo.setInterval(intervalKey);
        vo.setList(list);
        log.info("历史数据查询完成: {} 条记录", list.size());
        return vo;
    }

    private void validateTimeRange(HistoryQueryDTO dto, String interval) {
        LocalDateTime now = LocalDateTime.now();
        if (dto.getStartTime().isAfter(now)) {
            throw BusinessException.badRequest("开始时间不能是未来时间");
        }
        if (dto.getEndTime().isAfter(now)) {
            throw BusinessException.badRequest("结束时间不能是未来时间");
        }
        if (!dto.getStartTime().isBefore(dto.getEndTime())) {
            throw BusinessException.badRequest("开始时间必须早于结束时间");
        }
        long days = ChronoUnit.DAYS.between(dto.getStartTime(), dto.getEndTime());
        if ("RAW".equals(interval) && days > HistoryQueryDTO.MAX_RAW_DAYS) {
            throw BusinessException.badRequest(
                    "RAW 模式最大查询范围为 " + HistoryQueryDTO.MAX_RAW_DAYS + " 天");
        }
        if (!"RAW".equals(interval) && days > HistoryQueryDTO.MAX_AGG_DAYS) {
            throw BusinessException.badRequest(
                    "聚合模式最大查询范围为 " + HistoryQueryDTO.MAX_AGG_DAYS + " 天");
        }
    }

    @Override
    public DeviceStatusVO queryStatus(String deviceCode) {
        log.info("查询设备实时状态: deviceCode={}", deviceCode);

        DeviceStatusVO status = redisService.getDeviceStatus(deviceCode);
        if (status == null) {
            // Redis 中没有缓存，设备离线
            log.info("设备离线或无数据: deviceCode={}", deviceCode);
            status = new DeviceStatusVO();
            status.setDeviceCode(deviceCode);
            status.setOnline(false);
        }

        return status;
    }
}
