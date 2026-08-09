# 智慧能源综合管理平台接口设计

## 1. 接口规范

采用 RESTful API 设计。

统一前缀：`/api`

返回格式：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

错误响应（通过 GlobalExceptionHandler 统一处理）：

```json
{
  "code": 400,
  "message": "参数校验失败: deviceCode: 设备编号不能为空",
  "data": null
}
```

## 2. 实时监控接口

### 获取概览统计

```
GET /api/dashboard/overview
```

返回：

```json
{
  "code": 0,
  "data": {
    "deviceCount": 2,
    "onlineCount": 2,
    "totalPower": 1450.0,
    "todayEnergy": 150.7
  }
}
```

### 获取设备实时状态

```
GET /api/dashboard/device-status
```

返回设备列表，含 deviceCode、voltage、current、power、energy、online、updateTime。

### 获取功率趋势

```
GET /api/dashboard/power-trend
```

返回最近 1 小时的功率数据点（collectTime、totalPower）。

## 3. 设备管理接口

### 查询设备列表

```
GET /api/devices?page=1&pageSize=10&keyword=&deviceType=&status=
```

返回分页结果：`{ records: [], total: N }`

### 查询设备详情

```
GET /api/devices/{id}
```

### 新增设备

```
POST /api/devices

{
  "deviceCode": "DEVICE003",
  "deviceName": "智能电表",
  "deviceType": "METER",
  "location": "主卧",
  "status": 1
}
```

deviceCode 限制为 `[A-Za-z0-9_-]+`，重复返回 400 错误。

### 更新设备

```
PUT /api/devices/{id}
```

请求体同新增。

### 删除设备

```
DELETE /api/devices/{id}
```

## 4. 能源数据接口

### 上报能源数据

```
POST /api/energy/data

{
  "deviceCode": "DEVICE001",
  "voltage": 220.5,
  "current": 5.0,
  "power": 1102.5,
  "energy": 100.5,
  "collectTime": "2026-08-09 10:00:00"
}
```

deviceCode 限制为 `[A-Za-z0-9_-]+`。

### 查询历史能源数据

```
GET /api/energy/history/{deviceCode}?hours=24&limit=100
```

hours 范围：1~168，limit 范围：1~1000。

### 查询设备实时状态（Redis）

```
GET /api/energy/status/{deviceCode}
```

## 5. 用户管理接口

### 查询用户列表

```
GET /api/users
```

返回所有用户（当前版本无分页、无认证）。

## 6. 后续扩展接口（未实现）

- `POST /api/user/login` — 用户登录
- `GET /api/alarm/list` — 告警查询
- `POST /api/alarm/confirm/{id}` — 告警确认
- `POST /api/device/report` — MQTT 设备上行

## 7. 错误码

| code | 说明 |
|------|------|
| 0 | 成功 |
| 400 | 参数校验失败 / 业务逻辑错误 |
| 404 | 资源不存在 |
| 500 | 系统内部错误 |
