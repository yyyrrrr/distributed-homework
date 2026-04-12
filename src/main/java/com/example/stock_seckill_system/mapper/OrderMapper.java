package com.example.stock_seckill_system.mapper;

import com.example.stock_seckill_system.model.Order;

public interface OrderMapper {
    void insert(Order order);
    Order selectById(Long id);
    Order selectByUserIdAndProductId(Long userId, Long productId);
    int updateStatus(Long orderId, String status);
}
