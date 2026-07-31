# 智慧能源综合管理平台架构设计

## 1. 系统架构概述

Smart Energy Platform 采用前后端分离架构设计。

整体包含：

- Web前端展示层
- 后端业务服务层
- 数据存储层
- 数据采集模拟层
- 基础运行环境


整体架构：
              用户浏览器

                  |
                  |

          Vue3 Web应用

                  |
                  |
             HTTP REST API

                  |
                  |

        Spring Boot 后端服务

                  |
    --------------------------------
    |              |               |

  MySQL         Redis          TDengine

业务数据       缓存数据        时序数据


                  |

          数据采集模拟服务

                  |

          模拟能源设备数据

# 2. 项目目录结构
smart-energy-platform
├── smart-energy-server
│
├── smart-energy-web
│
├── smart-energy-simulator
│
├── docker
│
├── sql
│
├── docs
│
├── README.md
│
└── docker-compose.yml

# 3. 后端服务设计


## 3.1 后端模块

项目：

smart-energy-server


技术：

- Java 17
- Spring Boot 3
- Maven


采用单体应用架构。

后续根据业务规模可以拆分微服务。


# 4. 后端代码结构
smart-energy-server

src/main/java

com.smartenergy

├── controller

├── service

│   └── impl

├── mapper

├── entity

├── dto

├── vo

├── config

├── task

├── exception

└── utils
## 各层职责


### Controller

负责：

- 接收HTTP请求
- 参数校验
- 返回接口结果


禁止：

- 编写业务逻辑


---

### Service

负责：

- 核心业务处理
- 数据转换
- 事务控制


---

### Mapper

负责：

- 数据库访问
- SQL执行


---

### Entity

对应数据库表结构。


---

### DTO

接口请求对象。


---

### VO

接口返回对象。



# 5. 前端架构设计


项目：

smart-energy-web


技术：

- Vue3
- TypeScript
- Vite
- Element Plus
- ECharts


目录：
smart-energy-web

src

├── api

├── views

├── components

├── router

├── store

├── utils

└── assets
# 6. 数据存储架构


## 6.1 MySQL


负责保存业务数据。


主要表：

用户表：

sys_user


设备表：

device


告警规则：

alarm_rule


系统配置：

system_config



## 6.2 TDengine


负责保存设备时序数据。


特点：

- 高写入性能
- 时间序列查询优化


保存：

energy_data


字段：

- device_id
- voltage
- current
- power
- energy
- collect_time



## 6.3 Redis


负责：

- 热点数据缓存
- 实时设备状态缓存
- 登录Token


例如：device:status:{deviceId}

# 7. 数据采集架构


当前阶段：

使用模拟采集服务。


模块：

smart-energy-simulator


职责：

周期生成设备数据。


例如：

每10秒产生：
设备编号:

DEVICE001

电压:

220V

电流:

5A

功率:

1100W

时间:

2026-xx-xx xx:xx:xx

发送方式：

HTTP API。


未来升级：
设备

|

MQTT

|

消息服务

|

Spring Boot

# 8. 数据流设计


完整数据流程：
  模拟设备
     |
     |
产生能源数据
     |
     |
数据采集服务
     |
     | 
Spring Boot接口
      |
      |
    数据处理		
       |
  |          |     
Mysql     TDengine
业务信息  时序数据 
     |     
Vue查询展示

# 9. 接口设计规范


采用 RESTful API。


示例：


查询设备：
GET /api/device/list

新增设备：
POST /api/device/add

查询实时数据：
GET /api/energy/current/{deviceId}

查询历史趋势：
GET /api/energy/history/{deviceId}

# 10. 开发原则


1. 优先完成业务闭环。

2. 不过度设计。

3. 保持模块边界清晰。

4. 数据库设计优先。

5. 所有功能必须支持后续扩展真实设备。


# 11. 后续演进方向


当前：

单体Spring Boot


未来：

可以拆分：
用户服务

设备服务

采集服务

告警服务

分析服务

支持：
- MQTT
- Kafka
- 微服务
- AI能源分析
