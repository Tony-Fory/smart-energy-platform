package com.smartenergy.controller;

import com.smartenergy.common.Result;
import com.smartenergy.mapper.DeviceMapper;
import com.smartenergy.service.DashboardService;
import com.smartenergy.vo.DashboardVO;
import com.smartenergy.vo.DeviceStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dashboard 实时监控接口
 *
 * @author smart-energy
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardVO.Overview> overview() {
        return Result.success(dashboardService.getOverview());
    }

    @GetMapping("/device-status")
    public Result<List<DeviceStatusVO>> deviceStatus() {
        return Result.success(dashboardService.getDeviceStatusList());
    }

    @GetMapping("/power-trend")
    public Result<DashboardVO.PowerTrend> powerTrend() {
        return Result.success(dashboardService.getPowerTrend());
    }
}
