package com.example.stock_seckill_system.service;

import com.example.stock_seckill_system.config.rabbitmq.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public void processPayment(Long orderId) {
        try {
            // 模拟支付处理
            boolean paymentSuccess = simulatePayment();

            // 构建支付消息
            Map<String, Object> paymentMessage = new HashMap<>();
            paymentMessage.put("orderId", orderId);
            paymentMessage.put("paymentStatus", paymentSuccess ? "SUCCESS" : "FAILED");

            // 发送支付消息到队列
            rabbitTemplate.convertAndSend(RabbitMQConfig.PAYMENT_EXCHANGE, RabbitMQConfig.PAYMENT_ROUTING_KEY, 
                objectMapper.writeValueAsString(paymentMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean simulatePayment() {
        // 模拟支付处理，这里可以替换为真实的支付接口调用
        return Math.random() > 0.1; // 90%的概率支付成功
    }
}