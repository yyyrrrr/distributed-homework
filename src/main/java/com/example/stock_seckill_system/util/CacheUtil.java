package com.example.stock_seckill_system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存工具类，处理缓存穿透、击穿、雪崩问题
 */
@Component
public class CacheUtil {

    private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存空值的过期时间（防止缓存穿透）
    private static final long NULL_VALUE_EXPIRE_TIME = 2;
    
    // 普通缓存过期时间
    private static final long DEFAULT_EXPIRE_TIME = 24;
    
    // 缓存空值的标记
    private static final String NULL_MARK = "NULL";

    /**
     * 获取缓存，如果不存在则从数据库加载（处理穿透、击穿、雪崩）
     *
     * @param key 缓存key
     * @param loader 数据加载函数（从数据库加载）
     * @return 缓存或数据库中的值
     */
    public <T> T getWithLoad(String key, Function<String, T> loader) {
        try {
            // 1. 尝试从缓存获取
            Object cachedValue = redisTemplate.opsForValue().get(key);
            
            if (cachedValue != null) {
                // 如果是空值标记（防止穿透），返回 null
                if (NULL_MARK.equals(cachedValue)) {
                    logger.debug("缓存得到空值标记，key: {}", key);
                    return null;
                }
                logger.debug("缓存命中，key: {}", key);
                return (T) cachedValue;
            }

            // 2. 缓存未命中，使用本地锁防止击穿（简单实现，生产环境建议使用分布式锁）
            synchronized (key.intern()) {
                // 再次检查缓存，防止重复加载
                cachedValue = redisTemplate.opsForValue().get(key);
                if (cachedValue != null) {
                    if (NULL_MARK.equals(cachedValue)) {
                        return null;
                    }
                    return (T) cachedValue;
                }

                // 3. 从数据库加载数据
                logger.debug("缓存未命中，从数据库加载，key: {}", key);
                T data = loader.apply(key);

                // 4. 写入缓存（处理穿透和雪崩）
                if (data == null) {
                    // 缓存空值，防止穿透
                    redisTemplate.opsForValue().set(key, NULL_MARK, NULL_VALUE_EXPIRE_TIME, TimeUnit.HOURS);
                    logger.info("缓存空值以防穿透，key: {}", key);
                } else {
                    // 缓存真实数据，使用随机过期时间防止雪崩
                    long expireTime = DEFAULT_EXPIRE_TIME + (long) (Math.random() * 4);
                    redisTemplate.opsForValue().set(key, data, expireTime, TimeUnit.HOURS);
                    logger.info("缓存数据，key: {}, expire: {} hours", key, expireTime);
                }

                return data;
            }
        } catch (Exception e) {
            logger.error("缓存操作失败，key: {}", key, e);
            // 缓存失败时降级到数据库加载
            return loader.apply(key);
        }
    }

    /**
     * 设置缓存
     */
    public void set(String key, Object value, long expireHours) {
        try {
            if (value == null) {
                redisTemplate.opsForValue().set(key, NULL_MARK, NULL_VALUE_EXPIRE_TIME, TimeUnit.HOURS);
            } else {
                // 随机加一点时间，防止雪崩
                long finalExpire = expireHours + (long) (Math.random() * 4);
                redisTemplate.opsForValue().set(key, value, finalExpire, TimeUnit.HOURS);
            }
        } catch (Exception e) {
            logger.error("设置缓存失败，key: {}", key, e);
        }
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                logger.info("缓存删除成功，key: {}", key);
            }
        } catch (Exception e) {
            logger.error("删除缓存失败，key: {}", key, e);
        }
    }

    /**
     * 获取缓存
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("获取缓存失败，key: {}", key, e);
            return null;
        }
    }

    /**
     * 清空所有缓存（谨慎使用）
     */
    public void flushAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushAll();
            logger.warn("已清空所有缓存");
        } catch (Exception e) {
            logger.error("清空缓存失败", e);
        }
    }
}
