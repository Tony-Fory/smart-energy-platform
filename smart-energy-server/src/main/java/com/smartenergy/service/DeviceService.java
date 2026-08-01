package com.smartenergy.service;

import com.smartenergy.dto.DeviceCreateDTO;
import com.smartenergy.vo.DeviceVO;

import java.util.List;

/**
 * 设备服务接口
 *
 * @author smart-energy
 */
public interface DeviceService {

    /**
     * 查询设备列表
     */
    List<DeviceVO> listDevices();

    /**
     * 根据ID查询设备详情
     */
    DeviceVO getDevice(Long id);

    /**
     * 新增设备
     */
    DeviceVO createDevice(DeviceCreateDTO dto);

    /**
     * 更新设备
     */
    DeviceVO updateDevice(Long id, DeviceCreateDTO dto);

    /**
     * 删除设备
     */
    void deleteDevice(Long id);
}
