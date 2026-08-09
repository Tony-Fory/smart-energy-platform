package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.dto.DeviceCreateDTO;
import com.smartenergy.entity.Device;
import com.smartenergy.exception.BusinessException;
import com.smartenergy.mapper.DeviceMapper;
import com.smartenergy.vo.DeviceVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DeviceService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceService 单元测试")
class DeviceServiceImplTest {

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private DeviceCreateDTO validDto;
    private Device existingDevice;

    @BeforeEach
    void setUp() {
        validDto = new DeviceCreateDTO();
        validDto.setDeviceCode("DEVICE003");
        validDto.setDeviceName("测试设备");
        validDto.setDeviceType("TV");
        validDto.setLocation("测试位置");
        validDto.setStatus(1);

        existingDevice = new Device();
        existingDevice.setId(1L);
        existingDevice.setDeviceCode("DEVICE001");
        existingDevice.setDeviceName("客厅电视机");
        existingDevice.setDeviceType("TV");
        existingDevice.setLocation("客厅");
        existingDevice.setStatus(1);
    }

    @Test
    @DisplayName("设备不存在时 getDevice 抛出 BusinessException")
    void shouldThrowWhenDeviceNotFound() {
        when(deviceMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deviceService.getDevice(999L));
        assertEquals(404, ex.getCode());
        assertTrue(ex.getMessage().contains("设备不存在"));
    }

    @Test
    @DisplayName("创建重复 deviceCode 设备时抛出 BusinessException")
    void shouldThrowWhenCreateDuplicateDeviceCode() {
        when(deviceMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deviceService.createDevice(validDto));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("设备编号已存在"));
    }

    @Test
    @DisplayName("更新时 deviceCode 被其他设备占用则抛出 BusinessException")
    void shouldThrowWhenUpdateDuplicateDeviceCode() {
        // selectById: 设备存在
        when(deviceMapper.selectById(1L)).thenReturn(existingDevice);
        // selectCount: 有其他设备占用此 deviceCode
        when(deviceMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deviceService.updateDevice(1L, validDto));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("设备编号已存在"));
    }

    @Test
    @DisplayName("更新不存在的设备抛出 BusinessException")
    void shouldThrowWhenUpdateNotFound() {
        when(deviceMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deviceService.updateDevice(999L, validDto));
        assertEquals(404, ex.getCode());
        assertTrue(ex.getMessage().contains("设备不存在"));
    }

    @Test
    @DisplayName("删除不存在的设备抛出 BusinessException")
    void shouldThrowWhenDeleteNotFound() {
        when(deviceMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deviceService.deleteDevice(999L));
        assertEquals(404, ex.getCode());
        assertTrue(ex.getMessage().contains("设备不存在"));
    }

    @Test
    @DisplayName("正常创建设备返回 DeviceVO")
    void shouldCreateDeviceSuccessfully() {
        when(deviceMapper.selectCount(any())).thenReturn(0L);

        DeviceVO result = deviceService.createDevice(validDto);

        assertNotNull(result);
        assertEquals("DEVICE003", result.getDeviceCode());
        assertEquals("测试设备", result.getDeviceName());
        assertEquals(1, result.getStatus());
    }
}
