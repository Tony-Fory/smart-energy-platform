package com.smartenergy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartenergy.common.PageResult;
import com.smartenergy.dto.DeviceCreateDTO;
import com.smartenergy.entity.Device;
import com.smartenergy.mapper.DeviceMapper;
import com.smartenergy.service.DeviceService;
import com.smartenergy.vo.DeviceVO;
import com.smartenergy.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public PageResult<DeviceVO> listDevices(int page, int pageSize,
                                            String keyword, String deviceType, Integer status) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();

        // keyword: 设备名称或设备编号模糊匹配
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Device::getDeviceName, keyword)
                    .or()
                    .like(Device::getDeviceCode, keyword));
        }
        // 设备类型精确匹配
        if (StringUtils.hasText(deviceType)) {
            wrapper.eq(Device::getDeviceType, deviceType);
        }
        // 状态精确匹配
        if (status != null) {
            wrapper.eq(Device::getStatus, status);
        }

        wrapper.orderByDesc(Device::getId);

        Page<Device> devicePage = deviceMapper.selectPage(Page.of(page, pageSize), wrapper);
        List<DeviceVO> vos = devicePage.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(vos, devicePage.getTotal());
    }

    @Override
    public DeviceVO getDevice(Long id) {
        Device device = deviceMapper.selectById(id);
        if (device == null) {
            throw BusinessException.notFound("设备不存在");
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
            throw BusinessException.notFound("设备不存在");
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
            throw BusinessException.notFound("设备不存在");
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
