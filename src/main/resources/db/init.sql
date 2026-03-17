CREATE DATABASE IF NOT EXISTS stock_seckill_system;

USE stock_seckill_system;

CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

-- 创建商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    seckill_stock INT NOT NULL DEFAULT 0,
    seckill_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    seckill_start_time DATETIME,
    seckill_end_time DATETIME,
    status INT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

-- 插入测试数据
INSERT INTO product (name, description, price, stock, seckill_stock, seckill_price, status, create_time, update_time) VALUES
('商品1', '这是商品1的描述', 99.99, 100, 50, 59.99, 1, NOW(), NOW()),
('商品2', '这是商品2的描述', 199.99, 200, 100, 149.99, 1, NOW(), NOW()),
('商品3', '这是商品3的描述', 299.99, 150, 80, 249.99, 1, NOW(), NOW());

-- 创建订单表（预留）
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
);