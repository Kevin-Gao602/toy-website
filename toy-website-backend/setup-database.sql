-- MySQL 数据库快速设置脚本
-- 使用方法：在 MySQL Command Line Client 或 MySQL Workbench 中执行此脚本

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS toydb 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE toydb;

-- 显示数据库信息
SELECT 'Database toydb created successfully!' AS Message;
SELECT DATABASE() AS CurrentDatabase;
SELECT DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM information_schema.SCHEMATA 
WHERE SCHEMA_NAME = 'toydb';

-- 注意：表结构将由 Spring Boot JPA 自动创建
-- 启动应用后，执行以下命令查看表：
-- SHOW TABLES;

