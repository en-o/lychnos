-- ============================================
-- 数据库升级脚本：为 tb_book_analyse 表添加 author 字段
-- ============================================
-- 说明：此脚本仅用于升级已有数据库
-- 如果是首次初始化数据库，请直接使用 init_book_analyse.sql
-- 执行日期: 2026-01-20
-- ============================================

-- 添加 author 字段
ALTER TABLE tb_book_analyse ADD COLUMN author VARCHAR(200) COMMENT '作者';

-- 为现有数据更新作者信息（可选，根据实际情况调整）
UPDATE tb_book_analyse SET author = '刘慈欣' WHERE title = '三体';
UPDATE tb_book_analyse SET author = '余华' WHERE title = '活着';
UPDATE tb_book_analyse SET author = '余华' WHERE title = '许三观卖血记';
UPDATE tb_book_analyse SET author = '东野圭吾' WHERE title = '解忧杂货店';
UPDATE tb_book_analyse SET author = '尤瓦尔·赫拉利' WHERE title = '人类简史';
UPDATE tb_book_analyse SET author = '达尼伊尔·格拉宁' WHERE title = '奇特的一生';
UPDATE tb_book_analyse SET author = '连城三紀彦' WHERE title = '宵待草夜情';
UPDATE tb_book_analyse SET author = '薄伽丘' WHERE title = '十日谈';
UPDATE tb_book_analyse SET author = '连城三紀彦' WHERE title = '一朵桔梗花';
