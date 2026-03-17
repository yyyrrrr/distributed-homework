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

-- 创建商品表（预留）
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL
);

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