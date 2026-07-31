# 项目代码规范


## 1. 后端规范


技术：

- Java17
- Spring Boot3


采用：

Controller

↓

Service

↓

Mapper

↓

Database



## 2. 命名规范


类：

大驼峰：
DeviceController
EnergyService

方法：

小驼峰：
queryDeviceList()
saveEnergyData()


## 3. Controller规范


Controller只负责：

- 接收参数
- 参数校验
- 返回结果


禁止：

- 写业务逻辑



## 4. Service规范


Service负责：

- 业务处理
- 数据转换
- 事务管理



## 5. 数据访问规范


Mapper只负责：

- SQL操作


禁止：

复杂业务判断。



## 6. 异常处理


统一异常：

GlobalExceptionHandler



统一返回：

Result<T>



## 7. Git规范


提交格式：


新增：
feat: add device module

修复：
fix: fix energy query bug

优化：
perf: optimize query performance
