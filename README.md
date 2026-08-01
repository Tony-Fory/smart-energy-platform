# Smart Energy Platform

家庭智慧能源管理平台，基于 Spring Boot + Vue 3 实现设备管理、能源数据采集、实时状态缓存、时序数据存储和能源监控分析。

平台模拟真实能源设备采集场景，通过设备模拟器产生电压、电流、功率等数据，经后端服务处理后，使用 Redis 保存设备实时状态，TDengine 存储历史时序数据，并通过 Vue3 + ECharts 实现能源监控 Dashboard。

## 项目结构

```
smart-energy-platform/
├── smart-energy-server/     # 后端服务（Java 17 + Spring Boot 3）
├── smart-energy-web/        # 前端应用（Vue 3 + TypeScript + Vite）
├── smart-energy-simulator/  # 数据模拟服务（第三阶段开发）
├── docker/                  # Docker 初始化脚本
├── sql/                     # 数据库脚本
├── docs/                    # 项目文档
└── docker-compose.yml       # 开发环境编排
```

## 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 18+
- Docker Desktop
- MySQL 8
- Redis 7
- TDengine 3.3

## 快速开始

### 1. 启动基础设施

```bash
cp .env.example .env
docker compose up -d
docker compose ps
```

### 2. 启动后端

```bash
cd smart-energy-server
mvn spring-boot:run
```

健康检查：http://localhost:8080/actuator/health

### 3. 启动前端

```bash
cd smart-energy-web
npm install
npm run dev
```

访问：http://localhost:5173


## 当前开发进度

当前版本：MVP v0.1.0

已完成：

### 基础设施

- Spring Boot 3 + Java 17 后端服务
- Vue 3 + TypeScript 前端项目
- Docker Compose 开发环境
- MySQL、Redis、TDengine 数据源接入


### 设备管理

- 设备信息管理
- 设备 CRUD 接口
- 设备类型管理


### 能源数据采集

完成数据采集闭环：
设备模拟器
    ↓
HTTP数据上报
    ↓
Spring Boot服务
    ↓
TDengine时序存储


支持：

- 电压
- 电流
- 功率
- 能耗


### 实时监控

Dashboard 已实现：

- 设备数量统计
- 在线设备统计
- 实时功率展示
- 功率趋势曲线
- 设备实时状态


### 数据存储

- MySQL：业务数据
- Redis：设备实时状态缓存
- TDengine：历史能源数据


## 核心接口

### Dashboard

获取能源概览：
GET /api/dashboard/overview

获取设备实时状态：
GET /api/dashboard/device-status

获取功率趋势：
GET /api/dashboard/power-trend


## 项目进度

当前版本：MVP v0.1.0

智慧能源综合管理平台

基础设施
├── Spring Boot ✅
├── Vue3 ✅
├── MySQL ✅
├── Redis ✅
├── TDengine ✅
└── Docker Compose ✅


设备侧
├── 模拟设备采集 ✅
├── 定时采集任务 ✅
├── HTTP数据上报 ✅
└── 数据接收接口 ✅


数据层
├── MySQL业务数据 ✅
├── TDengine时序数据 ✅
├── Redis实时状态缓存 ✅


展示层
├── Dashboard监控大屏 ✅
├── 实时功率曲线 ✅
├── 在线设备状态 ✅


管理能力
├── 用户管理 ⚠️
├── 登录认证 ❌
├── 权限控制 ❌
├── 告警管理 ❌
├── 设备配置页面 ❌
└── 历史报表 ❌


## 后续规划
- [ ] 用户登录认证 JWT
- [ ] RBAC 权限管理
- [ ] 设备管理前端页面
- [ ] 能耗历史报表
- [ ] 告警规则管理
- [ ] MQTT真实设备接入