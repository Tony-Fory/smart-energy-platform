package com.smartenergy.simulator.generator;

import com.smartenergy.simulator.model.EnergyData;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟设备数据生成器
 * <p>
 * 根据设备类型生成合理的电压、电流、功率数据：
 * - 电视机(TV)：     电压 220V，电流 2.0~5.0A，功率 440~1100W
 * - 电风扇(FAN)：     电压 220V，电流 0.5~2.0A，功率 110~440W
 */
@Component
public class DataGenerator {

    private final Map<String, Double> energyAccumulator = new ConcurrentHashMap<>();

    /**
     * 为指定设备生成一份能源数据
     */
    public EnergyData generate(String deviceCode, String deviceType) {
        double voltage = randomVoltage();
        double current = randomCurrent(deviceType);
        double power = voltage * current;
        double energy = accumulateEnergy(deviceCode, power);

        return EnergyData.builder()
                .deviceCode(deviceCode)
                .voltage(Math.round(voltage * 10.0) / 10.0)
                .current(Math.round(current * 100.0) / 100.0)
                .power(Math.round(power * 10.0) / 10.0)
                .energy(Math.round(energy * 100.0) / 100.0)
                .collectTime(LocalDateTime.now())
                .build();
    }

    /**
     * 电压：220V ± 5V
     */
    private double randomVoltage() {
        return 220.0 + ThreadLocalRandom.current().nextDouble(-5.0, 5.0);
    }

    /**
     * 电流：根据设备类型
     */
    private double randomCurrent(String deviceType) {
        return switch (deviceType) {
            case "TV" -> ThreadLocalRandom.current().nextDouble(2.0, 5.0);
            case "FAN" -> ThreadLocalRandom.current().nextDouble(0.5, 2.0);
            default -> ThreadLocalRandom.current().nextDouble(1.0, 3.0);
        };
    }

    /**
     * 累计电量（kWh）
     */
    private double accumulateEnergy(String deviceCode, double power) {
        // 每次采集累加 (power W * 10s) / 3600s / 1000 = energy in kWh
        double deltaKwh = (power * 10.0) / 3600_000.0;
        return energyAccumulator.merge(deviceCode, deltaKwh, Double::sum);
    }
}
