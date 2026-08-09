# Smart Energy Simulator

家庭能源设备数据模拟服务，周期性生成模拟的电压、电流、功率、能耗数据，通过 HTTP 上报到 smart-energy-server。

## 功能

- 每 10 秒（可配置）定时生成设备能源数据
- 支持 TV、FAN 等多种设备类型的模拟数据（电压 220V，电流按设备类型变化）
- 通过 HTTP POST 上报到后端 `/api/energy/data`
- 设备列表从 `application.yml` 的 `simulator.devices` 配置读取

## 启动

```bash
cd smart-energy-simulator
mvn spring-boot:run
```

默认运行在端口 8081，向 `http://localhost:8080` 上报数据。

## 配置

```yaml
simulator:
  server-url: http://localhost:8080
  interval: 10           # 采集周期（秒）
  devices:
    - deviceCode: DEVICE001
      deviceName: 客厅电视机
      deviceType: TV
    - deviceCode: DEVICE002
      deviceName: 卧室电风扇
      deviceType: FAN
```
