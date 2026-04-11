package com.example.stock_seckill_system.service;

import com.example.stock_seckill_system.config.rabbitmq.RabbitMQConfig;
import com.example.stock_seckill_system.model.SeckillMessage;
import com.example.stock_seckill_system.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeckillService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderService orderService;

    public void seckill(Long userId, Long productId) throws Exception {
        // 1. 检查是否已经秒杀过（幂等性）
        String userProductKey = "seckill:user:" + userId + ":product:" + productId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userProductKey))) {
            throw new RuntimeException("您已经秒杀过该商品");
        }

        // 2. 检查库存
        String stockKey = "seckill:stock:" + productId;
        Integer stock = (Integer) redisTemplate.opsForValue().get(stockKey);
        if (stock == null || stock <= 0) {
            throw new RuntimeException("商品库存不足");
        }

        // 3. 扣减库存
        Long decrement = redisTemplate.opsForValue().decrement(stockKey);
        if (decrement < 0) {
            // 库存不足，回滚
            redisTemplate.opsForValue().increment(stockKey);
            throw new RuntimeException("商品库存不足");
        }

        // 4. 标记用户已秒杀
        redisTemplate.opsForValue().set(userProductKey, "1");

        // 5. 发送秒杀消息到队列
        try {
            SeckillMessage message = new SeckillMessage();
            message.setUserId(userId);
            message.setProductId(productId);
            rabbitTemplate.convertAndSend(RabbitMQConfig.SEckill_EXCHANGE, RabbitMQConfig.SEckill_ROUTING_KEY, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            // 处理RabbitMQ连接失败的情况，确保秒杀流程不被中断
            System.err.println("RabbitMQ连接失败，直接创建订单: " + e.getMessage());
            // 直接创建订单，确保即使消息队列不可用也能完成秒杀
            orderService.createOrder(userId, productId);
        }
    }
}
