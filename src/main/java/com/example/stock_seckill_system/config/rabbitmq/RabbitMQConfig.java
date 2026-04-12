package com.example.stock_seckill_system.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SEckill_QUEUE = "seckill_queue";
    public static final String SEckill_EXCHANGE = "seckill_exchange";
    public static final String SEckill_ROUTING_KEY = "seckill.routing.key";
    
    public static final String PAYMENT_QUEUE = "payment_queue";
    public static final String PAYMENT_EXCHANGE = "payment_exchange";
    public static final String PAYMENT_ROUTING_KEY = "payment.routing.key";

    @Bean
    public Queue seckillQueue() {
        return new Queue(SEckill_QUEUE, true);
    }

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SEckill_EXCHANGE, true, false);
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue()).to(seckillExchange()).with(SEckill_ROUTING_KEY);
    }
    
    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue()).to(paymentExchange()).with(PAYMENT_ROUTING_KEY);
    }
}
