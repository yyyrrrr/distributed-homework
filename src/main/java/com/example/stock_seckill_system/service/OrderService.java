package com.example.stock_seckill_system.service;

import com.example.stock_seckill_system.model.Order;

public interface OrderService {
    Order createOrder(Long userId, Long productId);
    Order getOrderById(Long id);
    Order getOrderByUserIdAndProductId(Long userId, Long productId);
}
