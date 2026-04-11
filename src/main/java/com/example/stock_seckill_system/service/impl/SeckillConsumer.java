package com.example.stock_seckill_system.service.impl;

import com.example.stock_seckill_system.config.rabbitmq.RabbitMQConfig;
import com.example.stock_seckill_system.model.SeckillMessage;
import com.example.stock_seckill_system.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillConsumer {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.SEckill_QUEUE)
    public void handleSeckillMessage(String message) {
        try {
            SeckillMessage seckillMessage = objectMapper.readValue(message, SeckillMessage.class);
            orderService.createOrder(seckillMessage.getUserId(), seckillMessage.getProductId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
