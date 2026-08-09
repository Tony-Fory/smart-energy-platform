package com.smartenergy.simulator.task;

import com.smartenergy.simulator.generator.DataGenerator;
import com.smartenergy.simulator.model.EnergyData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 数据采集定时任务
 * <p>
 * 按配置周期定时生成设备数据，通过 HTTP 发送到后端服务。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCollectTask {

    private final RestTemplate restTemplate;
    private final DataGenerator dataGenerator;

    @Value("${simulator.server-url}")
    private String serverUrl;

    /**
     * 模拟设备列表：
     * deviceCode → deviceType
     * TODO: 下一轮通过 @ConfigurationProperties 从 application.yml 读取设备配置，移除硬编码。
     */
    private final List<Map<String, String>> devices = List.of(
            Map.of("deviceCode", "DEVICE001", "deviceType", "TV"),
            Map.of("deviceCode", "DEVICE002", "deviceType", "FAN")
    );

    @Scheduled(fixedDelayString = "${simulator.interval:10}000")
    public void collectAndReport() {
        for (Map<String, String> device : devices) {
            String deviceCode = device.get("deviceCode");
            String deviceType = device.get("deviceType");

            try {
                EnergyData data = dataGenerator.generate(deviceCode, deviceType);
                String url = serverUrl + "/api/energy/data";
                restTemplate.postForObject(url, data, String.class);
                log.info("上报数据: {} 功率={}W 电流={}A", deviceCode, data.getPower(), data.getCurrent());
            } catch (Exception e) {
                log.error("上报失败: {} - {}", deviceCode, e.getMessage());
            }
        }
    }
}
