package com.example.stock_seckill_system.service.impl;

import com.example.stock_seckill_system.mapper.ProductMapper;
import com.example.stock_seckill_system.model.Product;
import com.example.stock_seckill_system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String PRODUCT_LOCK_PREFIX = "product:lock:";
    private static final long CACHE_EXPIRE_TIME = 30L;
    private static final long LOCK_EXPIRE_TIME = 10L;

    private boolean isRedisAvailable() {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().get("test");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Product getProductById(Long id) {
        if (!isRedisAvailable()) {
            return productMapper.findById(id);
        }

        String key = PRODUCT_KEY_PREFIX + id;
        Product product = (Product) redisTemplate.opsForValue().get(key);

        if (product != null) {
            return product;
        }

        String lockKey = PRODUCT_LOCK_PREFIX + id;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_EXPIRE_TIME, TimeUnit.SECONDS);

        if (locked != null && locked) {
            try {
                product = productMapper.findById(id);

                if (product == null) {
                    redisTemplate.opsForValue().set(key, "", 5L, TimeUnit.MINUTES);
                    return null;
                }

                long expireTime = CACHE_EXPIRE_TIME + (long) (Math.random() * 60);
                redisTemplate.opsForValue().set(key, product, expireTime, TimeUnit.MINUTES);
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return getProductById(id);
        }

        return product;
    }

    @Override
    public boolean deductStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        if (product == null || product.getStock() < quantity) {
            return false;
        }

        int result = productMapper.updateStock(id, product.getStock() - quantity);
        if (result > 0) {
            if (isRedisAvailable()) {
                String key = PRODUCT_KEY_PREFIX + id;
                product.setStock(product.getStock() - quantity);
                redisTemplate.opsForValue().set(key, product, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean deductSeckillStock(Long id) {
        Product product = getProductById(id);
        if (product == null || product.getSeckillStock() <= 0) {
            return false;
        }

        int result = productMapper.updateSeckillStock(id, product.getSeckillStock() - 1);
        if (result > 0) {
            if (isRedisAvailable()) {
                String key = PRODUCT_KEY_PREFIX + id;
                product.setSeckillStock(product.getSeckillStock() - 1);
                redisTemplate.opsForValue().set(key, product, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            }
            return true;
        }

        return false;
    }
}
