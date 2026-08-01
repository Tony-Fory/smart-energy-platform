package com.smartenergy.service;

import com.smartenergy.vo.DashboardVO;
import com.smartenergy.vo.DeviceStatusVO;

import java.util.List;

/**
 * Dashboard 服务接口
 *
 * @author smart-energy
 */
public interface DashboardService {

    /**
     * 获取概览统计：设备总数、在线数、总功率、今日能耗
     */
    DashboardVO.Overview getOverview();

    /**
     * 获取所有设备实时状态
     */
    List<DeviceStatusVO> getDeviceStatusList();

    /**
     * 获取功率趋势（最近1小时，从 TDengine 查询超级表聚合数据）
     */
    DashboardVO.PowerTrend getPowerTrend();
}
