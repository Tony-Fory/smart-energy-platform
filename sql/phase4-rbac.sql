-- ============================================================
-- Phase 4: RBAC 权限控制 - 角色/权限/关联表
-- ============================================================
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- 引擎：InnoDB
-- ============================================================

USE smart_energy;

-- -----------------------------------------------------------
-- 1. 角色表 sys_role
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    role_code   VARCHAR(50)     NOT NULL                 COMMENT '角色编码',
    role_name   VARCHAR(100)    NOT NULL                 COMMENT '角色名称',
    status      TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -----------------------------------------------------------
-- 2. 用户角色关联表 sys_user_role
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    user_id     BIGINT          NOT NULL                 COMMENT '用户ID',
    role_id     BIGINT          NOT NULL                 COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- -----------------------------------------------------------
-- 3. 权限表 sys_permission
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    permission_code VARCHAR(100)    NOT NULL                 COMMENT '权限编码',
    permission_name VARCHAR(100)    NOT NULL                 COMMENT '权限名称',
    status          TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：0-禁用，1-启用',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- -----------------------------------------------------------
-- 4. 角色权限关联表 sys_role_permission
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    role_id         BIGINT          NOT NULL                 COMMENT '角色ID',
    permission_id   BIGINT          NOT NULL                 COMMENT '权限ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- ============================================================
-- 5. 初始化数据
-- ============================================================

-- 角色
INSERT INTO sys_role (role_code, role_name) VALUES
    ('ADMIN',    '管理员'),
    ('OPERATOR', '操作员'),
    ('VIEWER',   '观察者');

-- 权限
INSERT INTO sys_permission (permission_code, permission_name) VALUES
    ('DASHBOARD_VIEW',  'Dashboard 查看'),
    ('DEVICE_VIEW',     '设备查看'),
    ('DEVICE_CREATE',   '设备新增'),
    ('DEVICE_UPDATE',   '设备编辑'),
    ('DEVICE_DELETE',   '设备删除'),
    ('USER_VIEW',       '用户查看');

-- 角色-权限关联
-- ADMIN: 全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'ADMIN';

-- OPERATOR: DASHBOARD_VIEW, DEVICE_VIEW, DEVICE_CREATE, DEVICE_UPDATE
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'OPERATOR'
  AND p.permission_code IN ('DASHBOARD_VIEW', 'DEVICE_VIEW', 'DEVICE_CREATE', 'DEVICE_UPDATE');

-- VIEWER: DASHBOARD_VIEW, DEVICE_VIEW
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'VIEWER'
  AND p.permission_code IN ('DASHBOARD_VIEW', 'DEVICE_VIEW');

-- 用户-角色关联: admin → ADMIN
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN';
