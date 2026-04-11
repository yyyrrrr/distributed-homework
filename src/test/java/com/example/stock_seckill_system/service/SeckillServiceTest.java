package com.example.stock_seckill_system.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SeckillServiceTest {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 测试秒杀基本功能
     */
    @Test
    public void testSeckillBasic() throws Exception {
        Long userId = 1L;
        Long productId = 1L;
        String stockKey = "seckill:stock:" + productId;
        String userProductKey = "seckill:user:" + userId + ":product:" + productId;

        // 清除缓存
        redisTemplate.delete(stockKey);
        redisTemplate.delete(userProductKey);

        // 初始化库存
        redisTemplate.opsForValue().set(stockKey, 10);

        // 执行秒杀
        seckillService.seckill(userId, productId);

        // 验证库存是否减少
        Integer stock = (Integer) redisTemplate.opsForValue().get(stockKey);
        assertEquals(9, stock, "库存应该减少1");

        // 验证用户是否已秒杀
        Object userSeckill = redisTemplate.opsForValue().get(userProductKey);
        assertEquals("1", userSeckill, "用户应该被标记为已秒杀");

        System.out.println("✓ 秒杀基本功能测试通过");
    }

    /**
     * 测试秒杀幂等性
     */
    @Test
    public void testSeckillIdempotency() throws Exception {
        Long userId = 2L;
        Long productId = 1L;
        String stockKey = "seckill:stock:" + productId;
        String userProductKey = "seckill:user:" + userId + ":product:" + productId;

        // 清除缓存
        redisTemplate.delete(stockKey);
        redisTemplate.delete(userProductKey);

        // 初始化库存
        redisTemplate.opsForValue().set(stockKey, 10);

        // 第一次秒杀
        seckillService.seckill(userId, productId);

        // 第二次秒杀应该失败
        Exception exception = assertThrows(Exception.class, () -> {
            seckillService.seckill(userId, productId);
        });
        assertEquals("您已经秒杀过该商品", exception.getMessage(), "重复秒杀应该失败");

        // 验证库存只减少1
        Integer stock = (Integer) redisTemplate.opsForValue().get(stockKey);
        assertEquals(9, stock, "库存应该只减少1");

        System.out.println("✓ 秒杀幂等性测试通过");
    }

    /**
     * 测试库存不足时的秒杀
     */
    @Test
    public void testSeckillWithInsufficientStock() throws Exception {
        Long userId = 3L;
        Long productId = 1L;
        String stockKey = "seckill:stock:" + productId;
        String userProductKey = "seckill:user:" + userId + ":product:" + productId;

        // 清除缓存
        redisTemplate.delete(stockKey);
        redisTemplate.delete(userProductKey);

        // 初始化库存为0
        redisTemplate.opsForValue().set(stockKey, 0);

        // 秒杀应该失败
        Exception exception = assertThrows(Exception.class, () -> {
            seckillService.seckill(userId, productId);
        });
        assertEquals("商品库存不足", exception.getMessage(), "库存不足时秒杀应该失败");

        // 验证用户没有被标记为已秒杀
        Object userSeckill = redisTemplate.opsForValue().get(userProductKey);
        assertNull(userSeckill, "用户不应该被标记为已秒杀");

        System.out.println("✓ 库存不足时的秒杀测试通过");
    }
}
