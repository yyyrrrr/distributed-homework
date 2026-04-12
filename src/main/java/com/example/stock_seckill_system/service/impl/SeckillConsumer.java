package com.example.stock_seckill_system.service.impl;

import com.example.stock_seckill_system.config.rabbitmq.RabbitMQConfig;
import com.example.stock_seckill_system.model.SeckillMessage;
import com.example.stock_seckill_system.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeckillConsumer {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitMQConfig.SEckill_QUEUE)
    public void handleSeckillMessage(String message) {
        SeckillMessage seckillMessage = null;
        try {
            seckillMessage = objectMapper.readValue(message, SeckillMessage.class);
            orderService.createOrder(seckillMessage.getUserId(), seckillMessage.getProductId());
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常，回滚Redis中的预扣减
            if (seckillMessage != null) {
                String stockKey = "seckill:stock:" + seckillMessage.getProductId();
                redisTemplate.opsForValue().increment(stockKey);
                // 清除用户秒杀标记
                String userProductKey = "seckill:user:" + seckillMessage.getUserId() + ":product:" + seckillMessage.getProductId();
                redisTemplate.delete(userProductKey);
            }
        }
    }
}
