# 智慧能源综合管理平台数据库设计


## 1. 数据存储方案


系统采用多数据库设计：

- MySQL：存储业务数据
- Redis：缓存热点数据
- TDengine：存储设备时序数据


设计原则：

业务数据和采集数据分离。


---


# 2. MySQL数据库设计


## 2.1 用户表 sys_user


用途：

保存系统用户信息。


字段：

|字段|说明|
|-|-|
|id|主键|
|username|用户名|
|password|密码|
|nickname|昵称|
|status|状态|
|create_time|创建时间|



---


## 2.2 设备表 device


用途：

保存能源设备基础信息。


字段：

|字段|说明|
|-|-|
|id|设备ID|
|device_code|设备编号|
|device_name|设备名称|
|device_type|设备类型|
|location|安装位置|
|status|设备状态|
|create_time|创建时间|



设备类型：

- 智能插座
- 空调
- 压缩机



---


## 2.3 告警规则表 alarm_rule


用途：

配置设备异常规则。


字段：

|字段|说明|
|-|-|
|id|主键|
|device_id|设备ID|
|rule_type|规则类型|
|threshold|阈值|
|status|状态|



---


# 3. TDengine时序数据设计


## 3.1 能源采集数据


超级表：

energy_data


字段：


|字段|说明|
|-|-|
|ts|采集时间|
|voltage|电压|
|current|电流|
|power|功率|
|energy|累计电量|


标签：

|字段|说明|
|-|-|
|device_id|设备编号|
|device_type|设备类型|


示例数据：

设备：

DEVICE001


时间：

2026-07-31 10:00:00


电压：

220V


电流：

5A


功率：

1100W


---


# 4. Redis设计


## 设备实时状态


key:
device:status:{deviceId}

value:

```json
{
 "power":1100,
 "online":true,
 "time":"2026-07-31 10:00:00"
}

用途：

快速查询设备当前状态。

# 5. 数据查询场景

实时监控

查询：

Redis

历史趋势

查询：

TDengine

用户和设备管理
查询：

MySQL