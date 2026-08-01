package com.smartenergy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartenergy.vo.DeviceStatusVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

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
}
