package com.example.stock_seckill_system.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String SEckill_QUEUE = "seckill_queue";
    public static final String SEckill_EXCHANGE = "seckill_exchange";
    public static final String SEckill_ROUTING_KEY = "seckill.routing.key";

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
}
