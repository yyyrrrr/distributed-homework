package com.example.stock_seckill_system.service.impl;

import com.example.stock_seckill_system.config.rabbitmq.RabbitMQConfig;
import com.example.stock_seckill_system.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentConsumer {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handlePaymentMessage(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            Long orderId = jsonNode.get("orderId").asLong();
            String paymentStatus = jsonNode.get("paymentStatus").asText();

            if ("SUCCESS".equals(paymentStatus)) {
                boolean success = orderService.payOrder(orderId);
                if (!success) {
                    // 支付失败，记录日志
                    System.err.println("订单支付失败，订单ID: " + orderId);
                }
            } else {
                orderService.updateOrderStatus(orderId, "FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 可以添加重试机制或死信队列处理
        }
    }
}