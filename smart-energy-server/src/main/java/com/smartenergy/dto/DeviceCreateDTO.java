package com.smartenergy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 设备新增/更新请求
 *
 * @author smart-energy
 */
@Data
public class DeviceCreateDTO {

    @NotBlank(message = "设备编号不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "设备编号只能包含字母、数字、下划线和连字符")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @NotBlank(message = "设备类型不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "设备类型只能包含字母、数字、下划线和连字符")
    private String deviceType;

    private String location;

    private Integer status;
}
