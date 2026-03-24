package com.example.stock_seckill_system.service.impl;

import com.example.stock_seckill_system.config.datasource.ReadOnly;
import com.example.stock_seckill_system.mapper.ProductMapper;
import com.example.stock_seckill_system.model.Product;
import com.example.stock_seckill_system.service.ProductService;
import com.example.stock_seckill_system.util.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CacheUtil cacheUtil;

    private static final String PRODUCT_CACHE_PREFIX = "product:";

    @Override
    @ReadOnly
    public Product getProductById(Long id) {
        if (id == null || id <= 0) {
            logger.warn("无效的商品ID: {}", id);
            return null;
        }

        String cacheKey = PRODUCT_CACHE_PREFIX + id;
        logger.info("获取商品信息: id={}", id);

        // 使用缓存工具类获取数据，自动处理穿透、击穿、雪崩问题
        return cacheUtil.getWithLoad(cacheKey, key -> {
            logger.debug("从数据库加载商品: id={}", id);
            return productMapper.findById(id);
        });
    }

    @Override
    public boolean deductStock(Long id, Integer quantity) {
        logger.info("扣减库存: id={}, quantity={}", id, quantity);
        
        if (id == null || id <= 0 || quantity == null || quantity <= 0) {
            logger.warn("无效的扣减参数: id={}, quantity={}", id, quantity);
            return false;
        }

        // 写操作使用主库
        int result = productMapper.updateStock(id, -quantity);
        
        if (result > 0) {
            // 库存扣减成功，清除缓存
            String cacheKey = PRODUCT_CACHE_PREFIX + id;
            cacheUtil.delete(cacheKey);
            logger.info("库存扣减成功，清除缓存: id={}", id);
        }
        
        return result > 0;
    }

    @Override
    public boolean deductSeckillStock(Long id) {
        logger.info("扣减秒杀库存: id={}", id);
        
        if (id == null || id <= 0) {
            logger.warn("无效的商品ID: {}", id);
            return false;
        }

        // 写操作使用主库
        int result = productMapper.updateSeckillStock(id, -1);
        
        if (result > 0) {
            // 秒杀库存扣减成功，清除缓存
            String cacheKey = PRODUCT_CACHE_PREFIX + id;
            cacheUtil.delete(cacheKey);
            logger.info("秒杀库存扣减成功，清除缓存: id={}", id);
        }
        
        return result > 0;
    }
}
