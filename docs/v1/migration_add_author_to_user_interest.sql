-- ============================================
-- 数据库升级脚本：为 tb_user_interest 表添加 author 字段
-- ============================================
-- 说明：此脚本仅用于升级已有数据库
-- 执行日期: 2026-01-20
-- ============================================

-- 添加 author 字段（可选字段，用于前端显示）
ALTER TABLE tb_user_interest ADD COLUMN author VARCHAR(200) COMMENT '作者（冗余字段，用于前端显示，非必填）';
ALTER TABLE tb_user_interest ADD COLUMN themes json DEFAULT NULL COMMENT '书主题（冗余字段，用于前端显示，非必填）';

-- 注意：
-- 1. 此字段为可选字段，允许为空
-- 2. 新数据会在用户提交反馈时自动填充
-- 3. 历史数据的 author 字段将保持为 NULL，前端会根据需要从 BookAnalyse 表获取
-- 4. 此优化主要解决 N+1 查询问题，提升查询性能
