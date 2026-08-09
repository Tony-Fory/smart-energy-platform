package com.smartenergy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartenergy.vo.DeviceStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Redis 缓存服务
 * <p>
 * 封装设备实时状态的 Redis 读写操作，使用 Jackson 序列化，设置 TTL 5 分钟。
 *
 * @author smart-energy
 */
@Slf4j
@Service
public class RedisService {

    private static final String STATUS_KEY_PREFIX = "device:status:";
    private static final Duration STATUS_TTL = Duration.ofMinutes(5);
    /** SCAN 每批返回数量 */
    private static final int SCAN_COUNT = 100;

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 保存设备状态到 Redis，设置 TTL 5 分钟。
     */
    public void saveDeviceStatus(DeviceStatusVO status) {
        String key = STATUS_KEY_PREFIX + status.getDeviceCode();
        try {
            String json = objectMapper.writeValueAsString(status);
            stringRedisTemplate.opsForValue().set(key, json, STATUS_TTL);
            log.debug("Redis 写入成功: key={}, ttl={}", key, STATUS_TTL);
        } catch (JsonProcessingException e) {
            log.error("Redis 序列化失败: deviceCode={}", status.getDeviceCode(), e);
        }
    }

    /**
     * 从 Redis 读取设备状态。
     *
     * @param deviceCode 设备编号
     * @return 设备状态，如果 key 不存在（已过期或从未写入）返回 null
     */
    public DeviceStatusVO getDeviceStatus(String deviceCode) {
        String key = STATUS_KEY_PREFIX + deviceCode;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            log.debug("Redis 未命中: key={}", key);
            return null;
        }
        try {
            log.debug("Redis 命中: key={}", key);
            return objectMapper.readValue(json, DeviceStatusVO.class);
        } catch (JsonProcessingException e) {
            log.error("Redis 反序列化失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 获取所有在线设备的实时状态。
     * <p>
     * 使用 SCAN 命令逐批遍历 device:status:* 键，避免 KEYS 阻塞 Redis。
     * 每批扫描 {@value #SCAN_COUNT} 个键，扫描完成后关闭 cursor。
     *
     * @return 所有在线设备的实时状态列表
     */
    public List<DeviceStatusVO> getAllDeviceStatuses() {
        List<DeviceStatusVO> list = new ArrayList<>();

        Set<String> keys = scanKeys(STATUS_KEY_PREFIX + "*");
        if (keys.isEmpty()) {
            log.debug("Redis 无在线设备");
            return list;
        }

        for (String key : keys) {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json != null) {
                try {
                    DeviceStatusVO status = objectMapper.readValue(json, DeviceStatusVO.class);
                    list.add(status);
                } catch (JsonProcessingException e) {
                    log.error("Redis 反序列化失败: key={}", key, e);
                }
            }
        }
        log.debug("Redis 查询所有设备状态: {} 个在线设备", list.size());
        return list;
    }

    /**
     * 使用 SCAN 命令逐批遍历匹配 pattern 的键。
     * <p>
     * SCAN 是非阻塞的增量迭代命令，适用于生产环境。
     *
     * @param pattern Redis 键匹配模式
     * @return 匹配的键集合
     */
    Set<String> scanKeys(String pattern) {
        return stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keySet = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(SCAN_COUNT)
                    .build();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                cursor.forEachRemaining(key ->
                        keySet.add(new String(key, StandardCharsets.UTF_8)));
            } catch (Exception e) {
                log.error("Redis SCAN 遍历异常: pattern={}", pattern, e);
            }
            return keySet;
        });
    }
}
