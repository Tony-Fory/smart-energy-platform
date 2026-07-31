# 智慧能源综合管理平台接口设计


## 1. 接口规范


采用 RESTful API 设计。


统一前缀：
/api

返回格式：

```json
{
  "code":0,
  "message":"success",
  "data":{}
}

2. 用户接口

登录

POST
/api/user/login

请求：
{
 "username":"admin",
 "password":"123456"
}

返回：
{
 "token":"xxx"
}

3. 设备管理接口

查询设备列表

GET
/api/device/list

返回：
[
 {
  "deviceCode":"DEVICE001",
  "deviceName":"智能插座",
  "status":"ONLINE"
 }
]

新增设备

POST
/api/device/add

请求：
{
 "deviceCode":"DEVICE001",
 "deviceName":"客厅插座",
 "deviceType":"SOCKET"
}

4. 能源数据接口

查询实时数据

GET
/api/energy/current/{deviceCode}

返回：
{
 "voltage":220,
 "current":5,
 "power":1100,
 "energy":20.5
}

查询历史趋势

GET
/api/energy/history/{deviceCode}

参数：
startTime
endTime

返回：
[
 {
  "time":"2026-07-31 10:00:00",
  "power":1100
 }
]

5. 告警接口

查询告警：

GET
/api/alarm/list

确认告警：

POST
/api/alarm/confirm/{id}

6. 后续扩展接口

未来支持：

MQTT设备接入
/api/device/report

设备上传：
{
 "deviceCode":"DEVICE001",
 "voltage":220,
 "current":5,
 "power":1100
}
