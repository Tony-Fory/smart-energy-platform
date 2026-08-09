package com.smartenergy.service;

import com.smartenergy.vo.DeviceStatusVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisService 单元测试
 * <p>
 * 重点验证 SCAN 逻辑代替 KEYS 命令后功能正常。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RedisService 单元测试")
class RedisServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisService redisService;

    @BeforeEach
    void setUp() {
        redisService = new RedisService(stringRedisTemplate);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("scanKeys 使用 SCAN 命令返回匹配的键")
    void shouldScanKeysSuccessfully() {
        // Mock execute with explicit RedisCallback type
        when(stringRedisTemplate.execute(any(org.springframework.data.redis.core.RedisCallback.class)))
                .thenReturn(Set.of("device:status:DEVICE001", "device:status:DEVICE002"));

        Set<String> keys = redisService.scanKeys("device:status:*");

        assertEquals(2, keys.size());
        assertTrue(keys.contains("device:status:DEVICE001"));
        assertTrue(keys.contains("device:status:DEVICE002"));
    }

    @Test
    @DisplayName("scanKeys 无匹配键时返回空集合")
    void shouldReturnEmptyWhenNoKeysMatch() {
        when(stringRedisTemplate.execute(any(org.springframework.data.redis.core.RedisCallback.class)))
                .thenReturn(Collections.emptySet());

        Set<String> keys = redisService.scanKeys("device:status:*");

        assertTrue(keys.isEmpty());
    }

    @Test
    @DisplayName("getAllDeviceStatuses 通过 SCAN 获取所有在线设备")
    void shouldGetAllStatusesViaScan() {
        when(stringRedisTemplate.execute(any(org.springframework.data.redis.core.RedisCallback.class)))
                .thenReturn(Set.of("device:status:DEVICE001"));

        String json = "{\"deviceCode\":\"DEVICE001\",\"power\":1100.0,\"online\":true}";
        when(valueOperations.get("device:status:DEVICE001")).thenReturn(json);

        List<DeviceStatusVO> statuses = redisService.getAllDeviceStatuses();

        assertEquals(1, statuses.size());
        assertEquals("DEVICE001", statuses.get(0).getDeviceCode());
        assertEquals(1100.0, statuses.get(0).getPower());
        assertTrue(statuses.get(0).getOnline());
    }

    @Test
    @DisplayName("getAllDeviceStatuses 无在线设备时返回空列表")
    void shouldReturnEmptyListWhenScanReturnsNothing() {
        when(stringRedisTemplate.execute(any(org.springframework.data.redis.core.RedisCallback.class)))
                .thenReturn(Collections.emptySet());

        List<DeviceStatusVO> statuses = redisService.getAllDeviceStatuses();

        assertTrue(statuses.isEmpty());
    }
}
