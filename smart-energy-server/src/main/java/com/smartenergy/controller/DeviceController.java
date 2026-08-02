package com.smartenergy.controller;

import com.smartenergy.common.PageResult;
import com.smartenergy.common.Result;
import com.smartenergy.dto.DeviceCreateDTO;
import com.smartenergy.service.DeviceService;
import com.smartenergy.vo.DeviceVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 设备管理接口
 *
 * @author smart-energy
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/devices")
    public Result<PageResult<DeviceVO>> listDevices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Integer status) {
        PageResult<DeviceVO> result = deviceService.listDevices(page, pageSize, keyword, deviceType, status);
        return Result.success(result);
    }

    @GetMapping("/devices/{id}")
    public Result<DeviceVO> getDevice(@PathVariable Long id) {
        DeviceVO device = deviceService.getDevice(id);
        return Result.success(device);
    }

    @PostMapping("/devices")
    public Result<DeviceVO> createDevice(@Valid @RequestBody DeviceCreateDTO dto) {
        DeviceVO device = deviceService.createDevice(dto);
        return Result.success(device);
    }

    @PutMapping("/devices/{id}")
    public Result<DeviceVO> updateDevice(@PathVariable Long id,
                                         @Valid @RequestBody DeviceCreateDTO dto) {
        DeviceVO device = deviceService.updateDevice(id, dto);
        return Result.success(device);
    }

    @DeleteMapping("/devices/{id}")
    public Result<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return Result.success(null);
    }
}
