# 项目部署说明


## 1. 开发环境


操作系统：

macOS


运行：

Docker Desktop



## 2. Docker组件


包含：


MySQL

Redis

TDengine



## 3. 启动方式


启动所有服务：
docker compose up -d

查看：
docker ps

停止：
docker compose down

## 4. 数据持久化


数据库数据通过volume保存。


MySQL:
mysql_data

Redis:
redis_data

TDengine:
tdengine_data

## 5. 环境变量


配置：
.env

包含：

数据库账号

密码

端口


禁止提交敏感信息。
