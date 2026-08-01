-- ============================================================
-- Phase 3: 能源数据采集模块 - TDengine 超级表
-- ============================================================
-- 数据库：TDengine 3.3.2
-- 连接：RESTful JDBC (taos-jdbcdriver)
-- 说明：
--   1. 创建能源数据超级表 energy_data
--   2. 子表由应用层通过 INSERT INTO ... USING ... TAGS 自动创建
--   3. 子表命名规则：energy_data_{deviceCode}
--     例：energy_data_DEVICE001、energy_data_DEVICE002
-- ============================================================
USE smart_energy;
CREATE STABLE IF NOT EXISTS energy_data (
    ts TIMESTAMP,
    voltage DOUBLE,
    current DOUBLE,
    power DOUBLE,
    energy DOUBLE
) TAGS (
    device_id NCHAR(50),
    device_type NCHAR(30)
);
