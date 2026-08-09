# Smart Energy Web

智慧能源管理平台前端应用，基于 Vue 3 + TypeScript + Vite 构建。

## 技术栈

- Vue 3 + TypeScript
- Vite
- Element Plus（UI 组件）
- ECharts（图表）
- Pinia（状态管理）
- Vue Router（路由）
- Axios（HTTP 请求）

## 页面

| 路径 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | 项目入口 |
| `/dashboard` | 实时监控面板 | 设备总数/在线统计、实时功率、功率趋势折线图、设备状态表 |
| `/device` | 设备管理 | 设备 CRUD、分页查询、关键字/类型/状态筛选 |

## 启动

```bash
cd smart-energy-web
npm install
npm run dev
```

开发服务器默认运行在 `http://localhost:5173`，API 请求通过 Vite 代理转发到 `http://localhost:8080`。

## 构建

```bash
npm run build
```

产物输出到 `dist/` 目录。
