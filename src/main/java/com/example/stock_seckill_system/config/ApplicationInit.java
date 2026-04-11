package com.example.stock_seckill_system.config;

import com.example.stock_seckill_system.mapper.ProductMapper;
import com.example.stock_seckill_system.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ApplicationInit {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        // 加载商品库存到Redis
        List<Product> products = productMapper.selectAll();
        for (Product product : products) {
            String stockKey = "seckill:stock:" + product.getId();
            redisTemplate.opsForValue().set(stockKey, product.getStock());
        }
    }
}
