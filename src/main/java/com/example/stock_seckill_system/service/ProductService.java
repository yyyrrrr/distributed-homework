package com.example.stock_seckill_system.service;

import com.example.stock_seckill_system.model.Product;

public interface ProductService {
    Product getProductById(Long id);
    boolean deductStock(Long id, Integer quantity);
    boolean deductSeckillStock(Long id);
}