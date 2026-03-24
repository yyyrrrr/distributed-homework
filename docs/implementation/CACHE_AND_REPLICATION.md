# 分布式缓存和读写分离实现指南

## 一、分布式缓存实现

### 1. 缓存穿透防护
**问题**：大量请求访问不存在的数据，绕过缓存直接查询数据库

**解决方案**：
- 缓存空值，设置较短的过期时间（2小时）
- 使用特殊标记 `NULL` 表示缓存的空值
- 在 `CacheUtil.getWithLoad()` 中实现

```java
// 自动缓存空值
if (data == null) {
    redisTemplate.opsForValue().set(key, "NULL", 2, TimeUnit.HOURS);
}
```

### 2. 缓存击穿防护
**问题**：热点数据缓存失效，大量请求同时查询数据库

**解决方案**：
- 使用 synchronized 本地锁（简单场景）
- 生产环境建议使用分布式锁（Redis 或 Zookeeper）
- 双重检查机制

```java
synchronized (key.intern()) {
    // 再次检查缓存，防止重复加载
    Object cachedValue = redisTemplate.opsForValue().get(key);
    if (cachedValue != null) {
        return (T) cachedValue;
    }
    // 从数据库加载
}
```

### 3. 缓存雪崩防护
**问题**：大量缓存在同一时间过期，请求涌入数据库

**解决方案**：
- 缓存过期时间增加随机值（24-28 小时）
- 避免使用相同的过期时间
- 使用热点数据预热等策略

```java
long expireTime = DEFAULT_EXPIRE_TIME + (long) (Math.random() * 4);
redisTemplate.opsForValue().set(key, data, expireTime, TimeUnit.HOURS);
```

## 二、读写分离实现

### 1. 配置方式

在 `application.properties` 中配置从库地址：
```properties
# 从库 URL (本地测试时可与主库相同)
spring.datasource.slave-url=jdbc:mysql://localhost:3306/stock_seckill_system?...
```

### 2. 使用方式

在需要读取数据的方法上添加 `@ReadOnly` 注解：

```java
@Override
@ReadOnly  // 标记使用从库
public Product getProductById(Long id) {
    // 这个方法会自动使用从库
    return productMapper.findById(id);
}
```

写操作（不添加 @ReadOnly）默认使用主库：

```java
@Override
public boolean deductStock(Long id, Integer quantity) {
    // 这个方法默认使用主库
    return productMapper.updateStock(id, -quantity) > 0;
}
```

### 3. 实现原理

1. **DataSourceContextHolder**：使用 ThreadLocal 存储当前线程的数据源标记
2. **DynamicRoutingDataSource**：继承 AbstractRoutingDataSource，动态选择数据源
3. **DataSourceInterceptor**：AOP 拦截器，根据 @ReadOnly 注解切换数据源
4. **DataSourceConfig**：创建两个数据源并配置路由

## 三、缓存水合（Cache Hydration）

当写操作后，需要清除相关缓存，防止数据不一致：

```java
// 写操作使用主库
int result = productMapper.updateStock(id, -quantity);

if (result > 0) {
    // 清除缓存，等待下次查询时重新加载
    cacheUtil.delete(PRODUCT_CACHE_PREFIX + id);
}
```

## 四、本地测试

### 前提条件
1. MySQL 已安装并运行
2. Redis 已安装并运行（或启动 Docker 容器）
3. 数据库已初始化（执行 `init.sql`）

### 运行应用

```bash
# 启动应用
mvn spring-boot:run

# 应用将在 http://localhost:8080 运行
```

### 测试用例

运行测试类验证功能：

```bash
# 运行单个测试方法
mvn test -Dtest=ProductServiceTest#testCachePenetrationProtection

# 运行所有测试
mvn test -Dtest=ProductServiceTest
```

## 五、性能指标

### 缓存效率
- **缓存命中率**：通过 Redis INFO 命令查看
- **响应时间**：缓存命中时 < 1ms，数据库查询 5-20ms

### 读写分离
- **读操作**：自动路由到从库，减轻主库压力
- **写操作**：在主库执行，确保一致性

## 六、生产环境建议

1. **分布式锁**：用 Redis 或 Zookeeper 替代本地 synchronized 锁
2. **热点数据预热**：应用启动时预加载热点商品
3. **缓存监控**：集成 Redis 监控工具（Prometheus + Grafana）
4. **多从库**：配置多个从库实现负载均衡
5. **缓存预热**：业务高峰期前预热缓存
6. **容错机制**：Redis 宕机时自动降级到数据库

## 七、相关文件

- **缓存工具**：[CacheUtil.java](src/main/java/com/example/stock_seckill_system/util/CacheUtil.java)
- **读写分离配置**：
  - [DataSourceContextHolder.java](src/main/java/com/example/stock_seckill_system/config/DataSourceContextHolder.java)
  - [DynamicRoutingDataSource.java](src/main/java/com/example/stock_seckill_system/config/DynamicRoutingDataSource.java)
  - [DataSourceConfig.java](src/main/java/com/example/stock_seckill_system/config/DataSourceConfig.java)
  - [DataSourceInterceptor.java](src/main/java/com/example/stock_seckill_system/config/DataSourceInterceptor.java)
  - [ReadOnly.java](src/main/java/com/example/stock_seckill_system/config/ReadOnly.java)
- **服务实现**：[ProductServiceImpl.java](src/main/java/com/example/stock_seckill_system/service/impl/ProductServiceImpl.java)
- **测试类**：[ProductServiceTest.java](src/test/java/com/example/stock_seckill_system/service/ProductServiceTest.java)
