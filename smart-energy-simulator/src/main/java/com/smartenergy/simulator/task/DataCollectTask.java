package com.smartenergy.simulator.task;

import com.smartenergy.simulator.config.SimulatorProperties;
import com.smartenergy.simulator.generator.DataGenerator;
import com.smartenergy.simulator.model.EnergyData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 数据采集定时任务
 * <p>
 * 按配置周期定时生成设备数据，通过 HTTP 发送到后端服务。
 * 设备列表从 application.yml 的 simulator.devices 配置读取。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCollectTask {

    private final RestTemplate restTemplate;
    private final DataGenerator dataGenerator;
    private final SimulatorProperties simulatorProperties;

    @Scheduled(fixedDelayString = "#{simulatorProperties.interval * 1000}")
    public void collectAndReport() {
        String serverUrl = simulatorProperties.getServerUrl();

        for (SimulatorProperties.DeviceConfig device : simulatorProperties.getDevices()) {
            String deviceCode = device.getDeviceCode();
            String deviceType = device.getDeviceType();

            if (deviceCode == null || deviceType == null) {
                log.warn("跳过无效设备配置: {}", device);
                continue;
            }

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
