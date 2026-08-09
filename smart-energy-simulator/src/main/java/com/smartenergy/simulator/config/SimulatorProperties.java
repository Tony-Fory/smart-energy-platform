package com.smartenergy.simulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 模拟器配置属性
 * <p>
 * 从 application.yml 读取设备列表和数据上报配置。
 *
 * @author smart-energy
 */
@Data
@Component
@ConfigurationProperties(prefix = "simulator")
public class SimulatorProperties {

    /** 后端服务地址 */
    private String serverUrl = "http://localhost:8080";

    /** 数据采集周期（秒） */
    private int interval = 10;

    /** 模拟设备列表 */
    private List<DeviceConfig> devices = new ArrayList<>();

    @Data
    public static class DeviceConfig {
        private String deviceCode;
        private String deviceName;
        private String deviceType;
    }
}
