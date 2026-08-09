-- ============================================================
-- Phase 2: 设备管理模块 - 设备表
-- ============================================================
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- 引擎：InnoDB
-- 说明：创建设备表并插入测试设备
-- ============================================================

USE smart_energy;

-- -----------------------------------------------------------
-- 1. 设备表 device
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS device (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    device_code VARCHAR(50)     NOT NULL                 COMMENT '设备编号',
    device_name VARCHAR(100)    NOT NULL                 COMMENT '设备名称',
    device_type VARCHAR(30)     NOT NULL                 COMMENT '设备类型：TV-电视机，FAN-电风扇',
    location    VARCHAR(200)    DEFAULT NULL             COMMENT '安装位置',
    status      TINYINT         NOT NULL DEFAULT 1       COMMENT '设备状态：0-停用，1-启用',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_device_code (device_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备信息表';

-- -----------------------------------------------------------
-- 2. 插入测试设备（电视机、电风扇各一台）
-- -----------------------------------------------------------
INSERT INTO device (device_code, device_name, device_type, location, status) VALUES
    ('DEVICE001', '客厅电视机', 'TV',  '客厅', 1),
    ('DEVICE002', '卧室电风扇', 'FAN', '卧室', 1);
