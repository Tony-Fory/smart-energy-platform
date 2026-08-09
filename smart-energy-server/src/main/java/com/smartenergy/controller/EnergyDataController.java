package com.smartenergy.controller;

import com.smartenergy.common.Result;
import com.smartenergy.dto.EnergyDataDTO;
import com.smartenergy.service.EnergyDataService;
import com.smartenergy.vo.DeviceStatusVO;
import com.smartenergy.vo.EnergyHistoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 能源数据采集接口
 *
 * @author smart-energy
 */
@Tag(name = "能源数据", description = "能源数据采集与查询接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class EnergyDataController {

    private final EnergyDataService energyDataService;

    @Operation(summary = "上报能源数据")
    @PostMapping("/energy/data")
    public Result<Void> report(@Valid @RequestBody EnergyDataDTO dto) {
        energyDataService.save(dto);
        return Result.success(null);
    }

    @Operation(summary = "查询历史能源数据")
    @GetMapping("/energy/history/{deviceCode}")
    public Result<EnergyHistoryVO> history(
            @Parameter(description = "设备编号") @PathVariable String deviceCode,
            @Parameter(description = "查询最近N小时") @RequestParam(defaultValue = "24") @Min(1) @Max(168) int hours,
            @Parameter(description = "最大返回条数") @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit) {
        EnergyHistoryVO vo = energyDataService.queryHistory(deviceCode, hours, limit);
        return Result.success(vo);
    }

    @Operation(summary = "查询设备实时状态")
    @GetMapping("/energy/status/{deviceCode}")
    public Result<DeviceStatusVO> status(@Parameter(description = "设备编号") @PathVariable String deviceCode) {
        DeviceStatusVO vo = energyDataService.queryStatus(deviceCode);
        return Result.success(vo);
    }
}
