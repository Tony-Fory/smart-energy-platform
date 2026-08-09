package com.smartenergy.controller;

import com.smartenergy.common.PageResult;
import com.smartenergy.common.Result;
import com.smartenergy.dto.DeviceCreateDTO;
import com.smartenergy.service.DeviceService;
import com.smartenergy.vo.DeviceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 设备管理接口
 *
 * @author smart-energy
 */
@Tag(name = "设备管理", description = "设备 CRUD 接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "查询设备列表")
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

    @Operation(summary = "查询设备详情")
    @GetMapping("/devices/{id}")
    public Result<DeviceVO> getDevice(@Parameter(description = "设备ID") @PathVariable Long id) {
        DeviceVO device = deviceService.getDevice(id);
        return Result.success(device);
    }

    @Operation(summary = "新增设备")
    @PostMapping("/devices")
    public Result<DeviceVO> createDevice(@Valid @RequestBody DeviceCreateDTO dto) {
        DeviceVO device = deviceService.createDevice(dto);
        return Result.success(device);
    }

    @Operation(summary = "更新设备")
    @PutMapping("/devices/{id}")
    public Result<DeviceVO> updateDevice(@Parameter(description = "设备ID") @PathVariable Long id,
                                         @Valid @RequestBody DeviceCreateDTO dto) {
        DeviceVO device = deviceService.updateDevice(id, dto);
        return Result.success(device);
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/devices/{id}")
    public Result<Void> deleteDevice(@Parameter(description = "设备ID") @PathVariable Long id) {
        deviceService.deleteDevice(id);
        return Result.success(null);
    }
}
