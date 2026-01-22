-- ============================================
-- 数据库升级脚本：为 tb_user_info 表添加 roles 字段
-- ============================================
-- 说明：此脚本用于升级已有数据库，添加用户角色字段（JSON类型）
-- 执行日期: 2026-01-22
-- ============================================

-- 添加 roles 字段（JSON类型）
ALTER TABLE tb_user_info ADD COLUMN roles JSON COMMENT '用户角色列表';

-- 为现有用户设置默认角色
UPDATE tb_user_info SET roles = '["USER"]' WHERE roles IS NULL;

-- 示例：为特定用户设置管理员角色
-- UPDATE tb_user_info SET roles = '["USER","ADMIN"]' WHERE login_name = 'admin';

