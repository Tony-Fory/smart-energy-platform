package com.smartenergy.service;

import com.smartenergy.dto.EnergyDataDTO;
import com.smartenergy.vo.EnergyHistoryVO;
import com.smartenergy.vo.DeviceStatusVO;

/**
 * 能源数据服务接口
 *
 * @author smart-energy
 */
public interface EnergyDataService {

    /**
     * 保存能源数据
     */
    void save(EnergyDataDTO dto);

    /**
     * 查询设备历史能源数据
     *
     * @param deviceCode 设备编号
     * @param hours      查询最近多少小时的数据
     * @param limit      最大返回数量
     * @return 历史数据
     */
    EnergyHistoryVO queryHistory(String deviceCode, int hours, int limit);

    /**
     * 查询设备实时状态（从 Redis 缓存读取）
     *
     * @param deviceCode 设备编号
     * @return 设备实时状态
     */
    DeviceStatusVO queryStatus(String deviceCode);
}
