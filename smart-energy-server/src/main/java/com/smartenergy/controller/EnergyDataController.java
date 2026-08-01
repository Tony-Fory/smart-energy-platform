package com.smartenergy.controller;

import com.smartenergy.common.Result;
import com.smartenergy.dto.EnergyDataDTO;
import com.smartenergy.service.EnergyDataService;
import com.smartenergy.vo.DeviceStatusVO;
import com.smartenergy.vo.EnergyHistoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 能源数据采集接口
 *
 * @author smart-energy
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnergyDataController {

    private final EnergyDataService energyDataService;

    @PostMapping("/energy/data")
    public Result<Void> report(@Valid @RequestBody EnergyDataDTO dto) {
        energyDataService.save(dto);
        return Result.success(null);
    }

    @GetMapping("/energy/history/{deviceCode}")
    public Result<EnergyHistoryVO> history(
            @PathVariable String deviceCode,
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "100") int limit) {
        EnergyHistoryVO vo = energyDataService.queryHistory(deviceCode, hours, limit);
        return Result.success(vo);
    }

    @GetMapping("/energy/status/{deviceCode}")
    public Result<DeviceStatusVO> status(@PathVariable String deviceCode) {
        DeviceStatusVO vo = energyDataService.queryStatus(deviceCode);
        return Result.success(vo);
    }
}
