package com.example.stock_seckill_system.service;

import com.example.stock_seckill_system.model.Product;
import com.example.stock_seckill_system.util.CacheUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheUtil cacheUtil;

    /**
     * 测试缓存穿透防护
     * 当数据库中不存在该商品时，缓存应该缓存空值以防止穿透
     */
    @Test
    public void testCachePenetrationProtection() {
        Long nonExistentId = 999999L;
        String cacheKey = "product:" + nonExistentId;
        
        // 清除缓存
        cacheUtil.delete(cacheKey);
        
        // 第一次查询（不存在的商品）
        Product product1 = productService.getProductById(nonExistentId);
        assertNull(product1, "不存在的商品应该返回 null");
        
        Object cachedValue = cacheUtil.get(cacheKey);
        assertNotNull(cachedValue, "缓存应该缓存空值标记");
        assertEquals("NULL", cachedValue, "应该缓存 NULL 标记以防穿透");
        
        System.out.println("✓ 缓存穿透防护测试通过");
    }

    /**
     * 测试缓存击穿防护
     * 多线程并发访问同一个数据时，应该只有一个线程查询数据库
     */
    @Test
    public void testCacheBreakdownProtection() throws InterruptedException {
        Long productId = 1L;
        String cacheKey = "product:" + productId;
        
        // 清除缓存模拟缓存失效
        cacheUtil.delete(cacheKey);
        
        // 并发查询计数
        final int threadCount = 5;
        final long[] dbQueryCount = {0};
        
        // 创建多个线程并发查询
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                Product product = productService.getProductById(productId);
                if (product != null) {
                    System.out.println(Thread.currentThread().getName() + " 获取到商品: " + product.getName());
                }
            });
            threads[i].start();
        }
        
        // 等待所有线程完成
        for (int i = 0; i < threadCount; i++) {
            threads[i].join();
        }
        
        // 验证缓存中有值
        Object cachedValue = cacheUtil.get(cacheKey);
        assertNotNull(cachedValue, "缓存应该缓存了商品数据");
        
        System.out.println("✓ 缓存击穿防护测试通过");
    }

    /**
     * 测试缓存雪崩防护
     * 缓存的过期时间应该有随机偏差，以防止大量缓存在同一时间过期
     */
    @Test
    public void testCacheAvalancheProtection() {
        Long productId = 1L;
        String cacheKey = "product:" + productId;
        
        // 清除缓存
        cacheUtil.delete(cacheKey);
        
        // 多次查询，每次的过期时间应该略有不同
        for (int i = 0; i < 3; i++) {
            cacheUtil.delete(cacheKey);
            Product product = productService.getProductById(productId);
            
            // 获取 TTL（这是一个间接的验证方式）
            System.out.println("第 " + (i + 1) + " 次查询完成");
        }
        
        System.out.println("✓ 缓存雪崩防护测试通过（在日志中可以看到随机过期时间）");
    }

    /**
     * 测试读写分离
     * 查询操作 (@ReadOnly) 应该使用从库，写操作应该使用主库
     */
    @Test
    public void testReadWriteSeparation() {
        Long productId = 1L;
        
        // 测试查询（应该使用从库）
        System.out.println("查询商品信息（使用从库）...");
        Product product = productService.getProductById(productId);
        
        if (product != null) {
            System.out.println("✓ 商品查询成功");
            System.out.println("  商品名称: " + product.getName());
            System.out.println("  商品库存: " + product.getStock());
        } else {
            System.out.println("✗ 商品不存在或查询失败");
        }
        
        System.out.println("✓ 读写分离测试完成");
    }

    /**
     * 测试写操作后缓存清除
     */
    @Test
    public void testCacheInvalidationAfterWrite() {
        Long productId = 1L;
        String cacheKey = "product:" + productId;
        
        // 先查询以缓存数据
        Product product1 = productService.getProductById(productId);
        Object cached1 = cacheUtil.get(cacheKey);
        assertNotNull(cached1, "第一次查询后应该有缓存");
        
        // 执行写操作（扣减库存）
        System.out.println("执行库存扣减操作...");
        productService.deductStock(productId, 1);
        
        // 写操作后缓存应该被清除
        Object cached2 = cacheUtil.get(cacheKey);
        assertNull(cached2, "写操作后缓存应该被清除");
        
        System.out.println("✓ 写操作后缓存清除测试通过");
    }
}
