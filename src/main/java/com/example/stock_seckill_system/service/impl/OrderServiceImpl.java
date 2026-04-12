package com.example.stock_seckill_system.service.impl;

import com.example.stock_seckill_system.mapper.OrderMapper;
import com.example.stock_seckill_system.mapper.ProductMapper;
import com.example.stock_seckill_system.model.Order;
import com.example.stock_seckill_system.model.Product;
import com.example.stock_seckill_system.service.OrderService;
import com.example.stock_seckill_system.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    @Override
    @Transactional
    public Order createOrder(Long userId, Long productId) {
        // 1. 检查是否已经下单（幂等性）
        Order existingOrder = orderMapper.selectByUserIdAndProductId(userId, productId);
        if (existingOrder != null) {
            return existingOrder;
        }

        // 2. 检查库存
        Product product = productMapper.findById(productId);
        if (product == null || product.getStock() <= 0) {
            // 库存不足，回滚Redis中的预扣减
            String stockKey = "seckill:stock:" + productId;
            redisTemplate.opsForValue().increment(stockKey);
            // 清除用户秒杀标记
            String userProductKey = "seckill:user:" + userId + ":product:" + productId;
            redisTemplate.delete(userProductKey);
            throw new RuntimeException("商品不存在或库存不足");
        }

        // 3. 扣减库存
        int result = productMapper.updateStock(productId);
        if (result == 0) {
            // 库存扣减失败，回滚Redis中的预扣减
            String stockKey = "seckill:stock:" + productId;
            redisTemplate.opsForValue().increment(stockKey);
            // 清除用户秒杀标记
            String userProductKey = "seckill:user:" + userId + ":product:" + productId;
            redisTemplate.delete(userProductKey);
            throw new RuntimeException("库存扣减失败");
        }

        // 4. 创建订单
        Order order = new Order();
        order.setId(idGenerator.nextId());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(1);
        order.setTotalPrice(product.getPrice());
        order.setStatus("PENDING");
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(order);

        return order;
    }

    @Override
    public Order getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public Order getOrderByUserIdAndProductId(Long userId, Long productId) {
        return orderMapper.selectByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional
    public boolean payOrder(Long orderId) {
        // 1. 检查订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }

        // 2. 检查订单状态是否为PENDING
        if (!"PENDING".equals(order.getStatus())) {
            return false;
        }

        // 3. 更新订单状态为PAID
        return updateOrderStatus(orderId, "PAID");
    }

    @Override
    @Transactional
    public boolean updateOrderStatus(Long orderId, String status) {
        int result = orderMapper.updateStatus(orderId, status);
        return result > 0;
    }
}
