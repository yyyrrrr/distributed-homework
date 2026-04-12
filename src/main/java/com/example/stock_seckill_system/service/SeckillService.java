package com.example.stock_seckill_system.service;

import com.example.stock_seckill_system.config.rabbitmq.RabbitMQConfig;
import com.example.stock_seckill_system.model.SeckillMessage;
import com.example.stock_seckill_system.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

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

    // Redis脚本：检查库存并扣减，同时标记用户已秒杀
    private final DefaultRedisScript<Long> seckillScript = new DefaultRedisScript<>();

    public SeckillService() {
        // 初始化Redis脚本
        seckillScript.setScriptText(
            "local stockKey = KEYS[1]\n" +
            "local userProductKey = KEYS[2]\n" +
            "\n" +
            "-- 检查用户是否已经秒杀过\n" +
            "if redis.call('exists', userProductKey) == 1 then\n" +
            "    return -1\n" +
            "end\n" +
            "\n" +
            "-- 检查库存\n" +
            "local stock = tonumber(redis.call('get', stockKey))\n" +
            "if not stock or stock <= 0 then\n" +
            "    return 0\n" +
            "end\n" +
            "\n" +
            "-- 扣减库存\n" +
            "redis.call('decr', stockKey)\n" +
            "\n" +
            "-- 标记用户已秒杀\n" +
            "redis.call('set', userProductKey, '1')\n" +
            "\n" +
            "return 1"
        );
        seckillScript.setResultType(Long.class);
    }

    public void seckill(Long userId, Long productId) throws Exception {
        String stockKey = "seckill:stock:" + productId;
        String userProductKey = "seckill:user:" + userId + ":product:" + productId;

        // 使用Redis脚本执行原子操作
        Long result = redisTemplate.execute(
            seckillScript,
            Arrays.asList(stockKey, userProductKey)
        );

        if (result == -1) {
            throw new RuntimeException("您已经秒杀过该商品");
        } else if (result == 0) {
            throw new RuntimeException("商品库存不足");
        }

        // 发送秒杀消息到队列
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
