package com.example.stock_seckill_system.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer seckillStock;
    private BigDecimal seckillPrice;
    private LocalDateTime seckillStartTime;
    private LocalDateTime seckillEndTime;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}