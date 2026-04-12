package com.example.stock_seckill_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.stock_seckill_system.mapper")
public class StockSeckillSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockSeckillSystemApplication.class, args);
	}

}
