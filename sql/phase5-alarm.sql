-- ============================================================
-- Phase 5: 告警管理模块
-- ============================================================
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- 引擎：InnoDB
-- ============================================================

USE smart_energy;

-- -----------------------------------------------------------
-- 1. 告警规则表 alarm_rule
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS alarm_rule (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    device_id   BIGINT          DEFAULT NULL             COMMENT '关联设备ID，NULL表示适用于所有设备',
    rule_name   VARCHAR(100)    NOT NULL                 COMMENT '规则名称',
    metric      VARCHAR(30)     NOT NULL                 COMMENT '监控指标：POWER/VOLTAGE/CURRENT',
    operator    VARCHAR(10)     NOT NULL                 COMMENT '运算符：GT/GTE/LT/LTE',
    threshold   DOUBLE          NOT NULL                 COMMENT '阈值',
    severity    VARCHAR(20)     NOT NULL DEFAULT 'WARNING' COMMENT '严重级别：INFO/WARNING/CRITICAL',
    status      TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_device_id (device_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

-- -----------------------------------------------------------
-- 2. 告警记录表 alarm_record
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS alarm_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    device_id       BIGINT          DEFAULT NULL             COMMENT '设备ID',
    device_code     VARCHAR(50)     DEFAULT NULL             COMMENT '设备编号',
    rule_id         BIGINT          DEFAULT NULL             COMMENT '触发规则ID',
    rule_name       VARCHAR(100)    DEFAULT NULL             COMMENT '触发规则名称',
    metric          VARCHAR(30)     NOT NULL                 COMMENT '监控指标',
    actual_value    DOUBLE          NOT NULL                 COMMENT '实际值',
    threshold       DOUBLE          NOT NULL                 COMMENT '阈值',
    severity        VARCHAR(20)     NOT NULL                 COMMENT '严重级别',
    status          TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0-未处理，1-已确认，2-已恢复',
    alarm_time      DATETIME        NOT NULL                 COMMENT '告警时间',
    recover_time    DATETIME        DEFAULT NULL             COMMENT '恢复时间',
    remark          VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_device_id (device_id),
    INDEX idx_rule_id (rule_id),
    INDEX idx_status (status),
    INDEX idx_alarm_time (alarm_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警记录表';

-- ============================================================
-- 3. RBAC 权限
-- ============================================================

-- 告警权限
INSERT INTO sys_permission (permission_code, permission_name) VALUES
    ('ALARM_RULE_VIEW',   '告警规则查看'),
    ('ALARM_RULE_CREATE', '告警规则新增'),
    ('ALARM_RULE_UPDATE', '告警规则编辑'),
    ('ALARM_RULE_DELETE', '告警规则删除'),
    ('ALARM_VIEW',        '告警记录查看'),
    ('ALARM_ACK',         '告警确认');

-- ADMIN: 全部告警权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'ADMIN'
  AND p.permission_code IN ('ALARM_RULE_VIEW','ALARM_RULE_CREATE','ALARM_RULE_UPDATE','ALARM_RULE_DELETE','ALARM_VIEW','ALARM_ACK');

-- OPERATOR: ALARM_RULE_VIEW/CREATE/UPDATE, ALARM_VIEW, ALARM_ACK
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'OPERATOR'
  AND p.permission_code IN ('ALARM_RULE_VIEW','ALARM_RULE_CREATE','ALARM_RULE_UPDATE','ALARM_VIEW','ALARM_ACK');

-- VIEWER: ALARM_VIEW
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'VIEWER'
  AND p.permission_code IN ('ALARM_VIEW');
