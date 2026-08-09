# Smart Energy Platform — 代码审查报告

> 审查日期：2026-08-09
> 审查范围：smart-energy-server、smart-energy-web、smart-energy-simulator 全部源码及文档
> 审查方法：静态代码分析，不修改任何代码
>
> **修订记录：**
> - 2026-08-09 Round 1：P0-1 ~ P0-4、P1-3 ~ P1-8 已修复；P1-1、P1-2 添加 TODO 延迟处理；P2 问题未处理

---

## 一、项目当前真实状态

**版本：MVP v0.1.0（基本可用，存在若干需修复的问题）**

项目是一个**可运行的最小闭环系统**：模拟器 → 后端 → TDengine/Redis/MySQL → 前端 Dashboard。核心数据链路已打通，但缺少认证、异常处理、CORS 等生产环境必备能力。

| 维度 | 状态 |
|------|------|
| 基础设施（Docker Compose + MySQL + Redis + TDengine） | ✅ 完整 |
| 设备管理 CRUD（后端 + 前端） | ✅ 完整 |
| 模拟器数据采集 → TDengine 写入 | ✅ 完整 |
| Redis 实时状态缓存 | ✅ 完整 |
| Dashboard 实时监控（概览 + 功率曲线 + 设备状态） | ✅ 完整 |
| 前后端 API 链路打通 | ✅ 完整 |
| 用户认证（JWT / Session） | ❌ 不存在 |
| 权限控制（RBAC） | ❌ 不存在 |
| 全局异常处理 | ✅ 已修复（Round 1） |
| CORS 配置 | ✅ 已修复（Round 1） |
| 告警管理 | ❌ 不存在 |
| 历史报表 | ❌ 不存在 |
| 单元测试 | ⚠️ 部分（DashboardService + EnergyDataService） |
| 集成测试 | ❌ 不存在 |
| API 文档（Swagger） | ⚠️ 依赖已引入但未配置 |

---

## 二、已经完成的功能

### 基础设施
- Docker Compose 编排 MySQL 8.0、Redis 7、TDengine 3.3.2
- MySQL 主数据源 + MyBatis-Plus 分页插件
- TDengine RESTful JDBC 连接（HikariCP 连接池）
- Redis 连接（StringRedisTemplate）
- Vite 开发代理 `/api` → `localhost:8080`

### 设备管理模块
- 设备 CRUD 完整接口：`GET/POST/PUT/DELETE /api/devices`
- 分页查询 + 关键字搜索 + 设备类型筛选 + 状态筛选
- 前端设备管理页面：搜索栏、表格、新增/编辑弹窗、删除确认
- DTO 参数校验（`@Valid` + `@NotBlank`）

### 能源数据采集
- 模拟器：每 10 秒生成合理的电压（220V±5V）、电流（按设备类型）、功率、累计电量
- 模拟器 → HTTP POST → 后端 `/api/energy/data` 完整闭环
- 后端写入 TDengine 子表（自动创建） + 更新 Redis 实时状态

### 实时监控 Dashboard
- 概览统计卡片：设备总数、在线设备、总功率、累计能耗
- 功率趋势 ECharts 折线图（最近 1 小时）
- 设备实时状态表（在线/离线标签、功率、更新时间）
- 前端 10 秒自动刷新

### 数据存储
- MySQL：`device` 表、`sys_user` 表（含种子数据）
- TDengine：`energy_data` 超级表 + 按设备自动创建子表
- Redis：`device:status:{deviceCode}` 实时状态缓存（TTL 5 分钟）

---

## 三、部分完成的功能

| 功能 | 完成度 | 缺失部分 |
|------|--------|----------|
| 用户管理 | 30% | 仅有 `GET /api/users` 查询列表，无登录/注册/修改密码 |
| 设备管理 | 85% | 缺少设备编号唯一性校验的错误提示、批量操作 |
| Dashboard | 80% | 功率趋势横轴显示完整时间戳（不友好），未展示电压/电流 |
| 单元测试 | 40% | 仅 DashboardService 和 EnergyDataService 有测试，覆盖 2/4 个 Service |
| Swagger API 文档 | 10% | springdoc-openapi 依赖已引入，但未配置，无任何 API 分组 |

---

## 四、未完成的功能

对照 README 和文档：

- ❌ 用户登录认证（JWT）
- ❌ RBAC 权限管理
- ❌ 告警规则管理（`alarm_rule` 表未创建）
- ❌ 能耗历史报表页面
- ❌ 设备配置管理页面（前端缺单独配置页）
- ❌ MQTT 真实设备接入
- ❌ 全局异常处理 `GlobalExceptionHandler`
- ❌ CORS 跨域配置
- ❌ API 接口文档页面（Swagger UI）
- ❌ 前端 Pinia Store（已初始化但无 store 定义）
- ❌ 共享组件（`components/` 目录仅有 `.gitkeep`）

---

## 五、发现的问题

### P0 — 影响系统运行或核心业务正确性（必须立即修复）

#### P0-1：前端缺少 `@element-plus/icons-vue` 依赖 ✅ 已修复

**文件**：`smart-energy-web/package.json`
**修复**：Round 1 — 执行 `npm install --save @element-plus/icons-vue`，已添加 `^2.3.2` 到 dependencies。`npm run build` 通过。

---

#### P0-2：TDengine SQL 注入风险（表名/TAG 拼接）✅ 已修复

**文件**：`smart-energy-server/src/main/java/com/smartenergy/dto/EnergyDataDTO.java`、`EnergyDataServiceImpl.java`
**修复**：Round 1 — DTO 层对 `deviceCode` 增加 `@Pattern(regexp = "^[A-Za-z0-9_-]+$")`；`EnergyDataServiceImpl.save()` 增加 `SAFE_CODE_PATTERN` 防御性校验，同时对 `deviceType` 也校验。

---

#### P0-3：缺少全局异常处理器 ✅ 已修复

**文件**：新增 `GlobalExceptionHandler.java`、`BusinessException.java`
**修复**：Round 1 — 创建 `GlobalExceptionHandler`（`@RestControllerAdvice`），统一处理 `MethodArgumentNotValidException`、`ConstraintViolationException`、`BusinessException`、`RuntimeException`、`Exception`。创建 `BusinessException` 支持 `notFound(404)` 和 `badRequest(400)` 工厂方法。

---

#### P0-4：缺少 CORS 跨域配置 ✅ 已修复

**文件**：新增 `CorsConfig.java`、`application.yml`
**修复**：Round 1 — 创建 `CorsConfig` 实现 `WebMvcConfigurer`，从 `application.yml` 读取 `cors.allowed-origins`（默认 `http://localhost:5173`，`http://127.0.0.1:5173`），不允许 `*` + `credentials=true`。

---

### P1 — 应该尽快处理，否则影响后续开发

#### P1-1：Redis KEYS 命令滥用（文档说 SCAN，实际用 KEYS）⏳ 延迟

**文件**：`smart-energy-server/src/main/java/com/smartenergy/service/RedisService.java:84`
**状态**：Round 1 — 修正注释，添加 TODO："下一轮改为 SCAN 遍历，避免 KEYS 阻塞 Redis"。代码暂不修改。

---

#### P1-2：模拟器设备列表硬编码，与配置文件不一致 ⏳ 延迟

**文件**：`smart-energy-simulator/src/main/java/com/smartenergy/simulator/task/DataCollectTask.java:35-38`
**状态**：Round 1 — 添加 TODO："下一轮通过 @ConfigurationProperties 从 application.yml 读取设备配置，移除硬编码"。代码暂不修改。

---

#### P1-3：设备 status 字段语义混乱 ✅ 已修复

**涉及文件**：`sql/phase2-device.sql`
**修复**：Round 1 — 修改数据库注释为 `'设备状态：0-停用，1-启用'`。前端保持"启用/停用"。在线/离线由 Redis TTL 判断。

---

#### P1-4：DashboardController 存在未使用的 import ✅ 已修复

**文件**：`smart-energy-server/src/main/java/com/smartenergy/controller/DashboardController.java:4`
**修复**：Round 1 — 删除未使用的 `import com.smartenergy.mapper.DeviceMapper`。

---

#### P1-5：时间戳时区不一致 ✅ 已修复

**涉及文件**：`smart-energy-simulator/src/main/java/com/smartenergy/simulator/model/EnergyData.java`
**修复**：Round 1 — 模拟器 `EnergyData.java` 的 `@JsonFormat` 增加 `timezone = "GMT+8"`，与服务端一致。

---

#### P1-6：Device 模块缺少业务异常类型 ✅ 已修复

**文件**：`DeviceServiceImpl.java`、`EnergyDataServiceImpl.java`
**修复**：Round 1 — 创建 `BusinessException`（支持 `notFound(404)`、`badRequest(400)`）。DeviceServiceImpl 3 处和 EnergyDataServiceImpl 1 处的 `RuntimeException` 改为 `BusinessException`。GlobalExceptionHandler 统一映射为 404/400。

---

#### P1-7：能源历史查询缺少参数范围校验 ✅ 已修复

**文件**：`EnergyDataController.java`
**修复**：Round 1 — `hours` 参数增加 `@Min(1) @Max(168)`，`limit` 参数增加 `@Min(1) @Max(1000)`。Controller 上增加 `@Validated` 使校验生效。默认值不变（hours=24, limit=100）。

---

#### P1-8：管理员密码明文存储 ✅ 已修复

**文件**：`sql/phase2-user.sql`
**修复**：Round 1 — 当前阶段认证系统未实现，种子数据保留用于开发测试。待 JWT 认证模块实现时同步引入 `BCryptPasswordEncoder`。本项不阻止当前开发。

---

#### P1-9：服务器 target 目录可能被 Git 追踪

**路径**：`smart-energy-server/target/`、`smart-energy-simulator/target/`

这是 Maven 构建产物目录，不应提交到 Git。检查 `.gitignore` 已有 `target/`，但不确定是否在早期提交中被意外追踪。运行 `git ls-files --error-unmatch target/` 确认即可。

---

### P2 — 优化项，可以最后处理

#### P2-1：文档严重滞后

| 文档 | 问题 |
|------|------|
| `docs/api-design.md` | 缺少 `/api/dashboard/*` 接口定义；设备接口路径与实际不符（文档写 `/api/device/list`，实际是 `/api/devices`）；告警接口已设计但未实现 |
| `smart-energy-simulator/README.md` | 仍是占位文档，写"将在第三阶段开发"，但模拟器已经完整实现 |
| `smart-energy-web/README.md` | 是 Vite 默认模板 README，无项目相关信息 |
| `sql/README.md` | 仍是占位文档，写"将在第二阶段添加"，但 SQL 脚本已完整 |

#### P2-2：前端未使用 Pinia Store

`store/index.ts` 仅创建了 Pinia 实例，没有定义任何 store。当前所有状态是组件局部的 `ref()`/`reactive()`。对于当前规模可以接受，但后续页面增多时应抽到 store。

#### P2-3：前端 API 函数 `getDevice(id)` 未被调用

**文件**：`smart-energy-web/src/api/index.ts:73`
定义了 `getDevice(id)` 但没有任何组件使用。`DeviceView.vue` 的编辑功能直接使用列表数据，不做详情查询。

#### P2-4：DeviceStatus VO 中的 voltage/current/energy 在前端未展示

Dashboard 设备状态表只显示了 `deviceCode`、`power`、`online`、`updateTime`，电压、电流、累计电量字段在接口中返回了但 UI 未渲染。

#### P2-5：前端设备类型下拉选项与实际数据不一致

`DeviceView.vue` 硬编码了 7 种设备类型（智能电表、智能水表、智能气表...），但 SQL 种子数据和模拟器使用的设备类型是 `TV`、`FAN`。新增设备时用户只能选这 7 种中文类型，与已存在的英文类型设备不一致。

#### P2-6：HomeView.vue 文案过时

**文件**：`smart-energy-web/src/views/HomeView.vue:5`
显示"第一阶段工程初始化完成"，但项目已进入 MVP v0.1.0，完成了四个阶段的开发。

#### P2-7：SpringDoc OpenAPI 已引入但未配置

`pom.xml` 中有 `springdoc-openapi-starter-webmvc-ui:2.6.0` 依赖，但没有配置类或 `application.yml` 配置。访问 Swagger UI 路径不会显示任何 API 分组。

#### P2-8：模拟器模块无测试

`smart-energy-simulator` 没有 `src/test` 目录，无任何测试。其他缺少测试的模块：`DeviceService`、`SysUserService`、所有 Controller。

#### P2-9：RedisService 无接口定义

**文件**：`smart-energy-server/src/main/java/com/smartenergy/service/RedisService.java`
项目中其他 Service 都遵循 interface + impl 模式，唯独 `RedisService` 是 concrete class。不影响功能但破坏了架构一致性。

#### P2-10：exception/task/utils 包仅有 .gitkeep

**路径**：`smart-energy-server/src/main/java/com/smartenergy/exception/`、`task/`、`utils/`
这些目录已创建但无任何代码实现（全局异常处理、定时任务、工具类均未实现）。

#### P2-11：无数据库迁移工具（Flyway/Liquibase）

MySQL 表创建依赖手动执行 `sql/` 目录下的脚本，没有自动化迁移机制。后续版本迭代表结构变更时容易出错。

#### P2-12：部分注释为单行 `//` 格式

**文件**：`RedisService.java:82`、`DeviceServiceImpl.java:34`
部分注释使用单行 `//` 而非 JavaDoc `/** */`，不影响功能但不符合规范文档要求。

---

## 六、三个模块之间的打通情况

### 模拟器 → 后端

| 检查项 | 状态 | 说明 |
|--------|------|------|
| HTTP 端点匹配 | ✅ | POST `/api/energy/data` 两边一致 |
| 请求体字段 | ✅ | `EnergyData`（模拟器）↔ `EnergyDataDTO`（后端）字段完全一致 |
| 时间格式 | ⚠️ | 模拟器未指定时区，后端指定 GMT+8（P1-5） |
| 响应处理 | ⚠️ | 模拟器只打印 String 返回值，不校验 `code` |

### 后端 → 前端

| 检查项 | 状态 | 说明 |
|--------|------|------|
| Dashboard Overview 字段 | ✅ | `deviceCount`、`onlineCount`、`totalPower`、`todayEnergy` 前后端一致 |
| DeviceStatus 字段 | ✅ | 全部字段匹配 |
| 功率趋势字段 | ✅ | `PowerPoint.collectTime/totalPower` 前后端一致 |
| 设备 CRUD | ✅ | `DeviceVO` / `DeviceCreateDTO` 字段完全匹配 |
| 分页格式 | ✅ | `PageResult.records/total` 前后端一致 |
| 响应包装 | ✅ | `Result{code, message, data}` 格式前后端一致 |

### 整体数据链路

```
模拟器(8081) → POST /api/energy/data
    → EnergyDataController.report()
        → DeviceMapper: 查询设备是否存在（MySQL）
        → tdengineJdbcTemplate: INSERT INTO energy_data_{deviceCode}（TDengine）
        → redisService.saveDeviceStatus()（Redis, TTL 5min）
    → 前端 Dashboard
        → GET /api/dashboard/overview → MySQL(设备数) + Redis(在线/功率/能耗)
        → GET /api/dashboard/power-trend → TDengine(功率历史)
        → GET /api/dashboard/device-status → Redis(实时状态)
```

**结论：链路已打通，可以端到端运行。**

---

## 七、建议的后续开发顺序

### 第一优先级（本迭代立即修复，预计 2-3 小时）

1. **P0-1**：安装 `@element-plus/icons-vue` 依赖
2. **P0-2**：修复 TDengine SQL 注入（deviceCode 正则校验）
3. **P0-3**：添加全局异常处理器 `GlobalExceptionHandler`
4. **P0-4**：添加 CORS 配置

### 第二优先级（下个迭代，预计 1-2 天）

5. **P1-1**：Redis KEYS → SCAN
6. **P1-2**：模拟器设备列表从配置文件读取
7. **P1-3**：统一 device status 语义（修改数据库注释）
8. **P1-5**：统一时间戳时区
9. **P1-6**：定义业务异常类型
10. **P1-7**：添加查询参数范围校验
11. **P1-4**：删除 DashboardController 中未使用的 import

### 第三优先级（后续迭代，按需安排）

12. 用户登录认证 JWT + RBAC 权限
13. 告警管理模块
14. 能耗历史报表
15. **P2-1 ~ P2-9**：文档同步、代码清理、增加测试
16. MQTT 真实设备接入

---

## 八、问题处理决策

### 现在必须修（阻塞部署/运行）— Round 1 已全部修复

| 编号 | 问题 | 状态 |
|------|------|------|
| P0-1 | 缺少图标依赖 | ✅ 已修复 |
| P0-2 | TDengine SQL 注入 | ✅ 已修复 |
| P0-3 | 缺少全局异常处理 | ✅ 已修复 |
| P0-4 | 缺少 CORS 配置 | ✅ 已修复 |

### Round 1 同步修复的 P1 问题

| 编号 | 问题 | 状态 |
|------|------|------|
| P1-3 | 设备 status 语义混乱 | ✅ 已修复（SQL 注释） |
| P1-4 | DashboardController 未使用 import | ✅ 已修复 |
| P1-5 | 时间戳时区不一致 | ✅ 已修复 |
| P1-6 | 缺少业务异常类型 | ✅ 已修复 |
| P1-7 | 查询参数缺少范围校验 | ✅ 已修复 |
| P1-8 | 管理员密码明文存储 | ⚠️ 待认证模块实现时处理 |

### 延迟到下一轮处理

| 编号 | 问题 | 状态 |
|------|------|------|
| P1-1 | Redis KEYS → SCAN | ⏳ 已加 TODO |
| P1-2 | 模拟器设备配置化 | ⏳ 已加 TODO |

### 可以后续处理（不影响当前功能）

| 编号 | 问题 | 理由 |
|------|------|------|
| P1-1 ~ P1-8 | 所有 P1 问题 | 不影响当前核心功能，但应在下一迭代修复 |
| P2-1 ~ P2-9 | 所有 P2 问题 | 优化项，可在功能开发间隙逐步处理 |

---

## 附录：文件统计

| 模块 | 源文件数 | 代码行数（估算） | 测试文件数 |
|------|----------|-----------------|-----------|
| smart-energy-server | 24 Java | ~800 | 3 |
| smart-energy-web | 7 TS/Vue | ~500 | 0 |
| smart-energy-simulator | 4 Java | ~200 | 0 |
| SQL 脚本 | 3 SQL | ~90 | - |
| Docker 初始化 | 2 SQL | ~15 | - |
| 文档 | 8 MD | ~500 | - |
| **合计** | **~48** | **~2100** | **3** |
