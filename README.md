# Smart Energy Platform

家庭智慧能源管理平台，基于 Spring Boot + Vue 3 实现设备管理、数据采集、能源监控和告警分析。

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

## 开发阶段

当前为 **第一阶段：项目初始化**，已完成工程脚手架与 Docker 环境配置，尚未开发业务模块。

详细计划见 [docs/development-plan.md](docs/development-plan.md)。

## 支持设备（家庭场景）

- 第一阶段：电视机（TV）、电风扇（FAN）
- 后续扩展：空调、冰箱、热水器、智能插座
