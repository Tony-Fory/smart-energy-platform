package com.smartenergy.service;

import com.smartenergy.common.PageResult;
import com.smartenergy.dto.DeviceCreateDTO;
import com.smartenergy.vo.DeviceVO;

/**
 * 设备服务接口
 *
 * @author smart-energy
 */
public interface DeviceService {

    /**
     * 分页查询设备列表
     *
     * @param page       页码
     * @param pageSize   每页条数
     * @param keyword    关键字（设备名称/编号模糊匹配）
     * @param deviceType 设备类型筛选
     * @param status     状态筛选
     */
    PageResult<DeviceVO> listDevices(int page, int pageSize, String keyword, String deviceType, Integer status);

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
