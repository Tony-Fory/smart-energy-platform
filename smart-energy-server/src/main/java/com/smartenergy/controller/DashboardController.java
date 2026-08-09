package com.smartenergy.controller;

import com.smartenergy.common.Result;
import com.smartenergy.service.DashboardService;
import com.smartenergy.vo.DashboardVO;
import com.smartenergy.vo.DeviceStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "实时监控", description = "Dashboard 实时监控接口")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取概览统计")
    @GetMapping("/overview")
    public Result<DashboardVO.Overview> overview() {
        return Result.success(dashboardService.getOverview());
    }

    @Operation(summary = "获取设备实时状态")
    @GetMapping("/device-status")
    public Result<List<DeviceStatusVO>> deviceStatus() {
        return Result.success(dashboardService.getDeviceStatusList());
    }

    @Operation(summary = "获取功率趋势")
    @GetMapping("/power-trend")
    public Result<DashboardVO.PowerTrend> powerTrend() {
        return Result.success(dashboardService.getPowerTrend());
    }
}
