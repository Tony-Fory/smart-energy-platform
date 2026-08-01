-- ============================================================
-- Phase 2: 用户模块 - 用户表
-- ============================================================
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- 引擎：InnoDB
-- 说明：创建用户表并插入测试用户
-- ============================================================

USE smart_energy;

-- -----------------------------------------------------------
-- 1. 用户表 sys_user
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    username    VARCHAR(50)     NOT NULL                 COMMENT '用户名',
    password    VARCHAR(255)    NOT NULL                 COMMENT '密码（加密存储）',
    nickname    VARCHAR(50)     DEFAULT NULL             COMMENT '昵称',
    status      TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- -----------------------------------------------------------
-- 2. 插入测试用户 admin
--    密码：admin123（明文示例，后续实现时需替换为加密密码）
-- -----------------------------------------------------------
INSERT INTO sys_user (username, password, nickname, status) VALUES
    ('admin', 'admin123', '管理员', 1);
