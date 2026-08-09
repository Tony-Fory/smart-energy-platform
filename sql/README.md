# SQL Scripts

数据库初始化脚本，按开发阶段组织。

## 脚本清单

| 脚本 | 说明 |
|------|------|
| `phase2-user.sql` | 系统用户表 `sys_user` + 测试用户 admin |
| `phase2-device.sql` | 设备表 `device` + 测试设备 DEVICE001/DEVICE002 |
| `phase3-energy-data.sql` | TDengine 超级表 `energy_data` |

## 执行方式

MySQL 脚本在 Docker 容器首次启动时通过 `docker/mysql/init/` 自动执行。

TDengine 脚本在 Docker 容器首次启动时通过 `docker/tdengine/init/` 自动执行。

也可以手动执行：

```bash
# MySQL
docker exec -i smart-energy-mysql mysql -u smart_energy -psmart_energy123 smart_energy < sql/phase2-device.sql

# TDengine
docker exec -i smart-energy-tdengine taos -s "source /docker-entrypoint-initdb.d/01-init.sql"
```

## 当前状态

已实现表：

- MySQL: `sys_user`、`device`
- TDengine: `energy_data` 超级表（子表按设备自动创建）
- 未实现表：`alarm_rule`、`system_config`（后续版本）

## 版本对应

- MVP v0.1.0: phase2-user.sql, phase2-device.sql, phase3-energy-data.sql
