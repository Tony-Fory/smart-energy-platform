package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartenergy.dto.DeviceCreateDTO;
import com.smartenergy.entity.Device;
import com.smartenergy.mapper.DeviceMapper;
import com.smartenergy.service.DeviceService;
import com.smartenergy.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备服务实现
 *
 * @author smart-energy
 */
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceMapper deviceMapper;

    @Override
    public List<DeviceVO> listDevices() {
        List<Device> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>().orderByDesc(Device::getId));
        return devices.stream().map(this::toVO).toList();
    }

    @Override
    public DeviceVO getDevice(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        return toVO(device);
    }

    @Override
    @Transactional
    public DeviceVO createDevice(DeviceCreateDTO dto) {
        Device device = new Device();
        device.setDeviceCode(dto.getDeviceCode());
        device.setDeviceName(dto.getDeviceName());
        device.setDeviceType(dto.getDeviceType());
        device.setLocation(dto.getLocation());
        device.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        deviceMapper.insert(device);
        return toVO(device);
    }

    @Override
    @Transactional
    public DeviceVO updateDevice(Long id, DeviceCreateDTO dto) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        device.setDeviceCode(dto.getDeviceCode());
        device.setDeviceName(dto.getDeviceName());
        device.setDeviceType(dto.getDeviceType());
        device.setLocation(dto.getLocation());
        if (dto.getStatus() != null) {
            device.setStatus(dto.getStatus());
        }
        deviceMapper.updateById(device);
        return toVO(device);
    }

    @Override
    @Transactional
    public void deleteDevice(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        deviceMapper.deleteById(id);
    }

    private DeviceVO toVO(Device device) {
        DeviceVO vo = new DeviceVO();
        vo.setId(device.getId());
        vo.setDeviceCode(device.getDeviceCode());
        vo.setDeviceName(device.getDeviceName());
        vo.setDeviceType(device.getDeviceType());
        vo.setLocation(device.getLocation());
        vo.setStatus(device.getStatus());
        vo.setCreateTime(device.getCreateTime());
        vo.setUpdateTime(device.getUpdateTime());
        return vo;
    }
}
