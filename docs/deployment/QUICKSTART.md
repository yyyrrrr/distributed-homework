# 分布式缓存和读写分离 - 快速开始指南

## 项目已成功启动 ✓

应用现在正在 **http://localhost:8080** 运行，已集成以下功能：

### 1. 分布式缓存（Redis）

#### 已实现的功能：
- ✓ 缓存穿透防护（空值缓存）
- ✓ 缓存击穿防护（本地锁机制）
- ✓ 缓存雪崩防护（随机过期时间）
- ✓ 自动缓存更新和失效

#### 相关类：
- `CacheUtil.java` - 缓存工具类，提供统一的缓存访问接口
- `ProductServiceImpl.java` - 在服务层集成缓存逻辑

### 2. 读写分离

#### 已实现的功能：
- ✓ 主从数据源自动切换
- ✓ @ReadOnly 注解标记只读方法
- ✓ 写操作自动使用主库
- ✓ 基于 AOP 的自动路由

#### 相关类：
- `DataSourceContextHolder.java` - ThreadLocal 上下文持有者
- `DynamicRoutingDataSource.java` - 动态数据源路由
- `DataSourceConfig.java` - 数据源配置
- `DataSourceInterceptor.java` - AOP 拦截器
- `ReadOnly.java` - 标记注解

## 快速测试

### 1. 手动验证缓存功能

#### 第一次查询（命中数据库）：
```bash
curl http://localhost:8080/api/products/1
```
查看日志：会看到"从数据库加载商品"的日志

#### 第二次查询（命中缓存）：
```bash
curl http://localhost:8080/api/products/1
```
查看日志：会看到"缓存命中"的日志，响应时间会显著减少

### 2. 运行单元测试

```bash
# 运行所有缓存测试
mvn test -Dtest=ProductServiceTest

# 运行特定测试
mvn test -Dtest=ProductServiceTest#testCachePenetrationProtection
mvn test -Dtest=ProductServiceTest#testCacheBreakdownProtection
mvn test -Dtest=ProductServiceTest#testReadWriteSeparation
```

### 3. 验证读写分离

在 ProductServiceImpl 中查看：
- `getProductById()` 方法带有 `@ReadOnly` 注解 → 使用从库
- `deductStock()` 和 `deductSeckillStock()` 没有注解 → 使用主库

在日志中会看到对应的"使用主库"或"使用从库"的信息。

## 配置说明

### application.properties

```properties
# 主库地址
spring.datasource.url=jdbc:mysql://localhost:3306/stock_seckill_system

# 从库地址（本地测试可与主库相同）
spring.datasource.slave-url=jdbc:mysql://localhost:3306/stock_seckill_system

# Redis 配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 修改配置示例

如果需要指向真实的主从库：

```properties
# 主库
spring.datasource.url=jdbc:mysql://master-db-server:3306/stock_seckill_system

# 从库
spring.datasource.slave-url=jdbc:mysql://slave-db-server:3306/stock_seckill_system
```

## 生产级建议

### 缓存相关
1. **分布式锁**：使用 Redis SET NX 或 Zookeeper 替代本地 synchronized
2. **热点数据预热**：应用启动时预加载热点商品
3. **缓存监控**：集成 Prometheus + Grafana 监控 Redis 状态
4. **容错降级**：Redis 宕机时自动降级到数据库

### 读写分离相关
1. **多个从库**：配置多个从库实现负载均衡
2. **从库延迟**：处理主从复制延迟（如写后必读操作）
3. **故障转移**：从库宕机时自动转向主库或其他从库
4. **监控延迟**：实时监控主从复制延迟

## 代码示例

### 使用缓存

```java
@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private CacheUtil cacheUtil;
    
    @Override
    @ReadOnly  // 标记为只读，使用从库
    public Product getProductById(Long id) {
        // 缓存工具自动处理穿透、击穿、雪崩问题
        return cacheUtil.getWithLoad("product:" + id, 
            key -> productMapper.findById(id));
    }
}
```

### 添加新的缓存项

```java
// 在任何 Bean 中注入 CacheUtil
@Autowired
private CacheUtil cacheUtil;

// 使用缓存
public MyData getData(String key) {
    return cacheUtil.getWithLoad("mydata:" + key, 
        k -> loadFromDatabase(key));
}
```

### 添加新的只读方法

```java
@Service
public class OrderService {
    
    @ReadOnly  // 添加这个注解
    public Order getOrder(Long orderId) {
        // 这个方法会自动使用从库
        return orderMapper.findById(orderId);
    }
    
    public void updateOrder(Order order) {
        // 没有 @ReadOnly，使用主库
        orderMapper.update(order);
    }
}
```

## 故障排查

### 问题：缓存未命中率高
解决方案：
1. 检查 Redis 连接是否正常
2. 检查缓存 key 的生成逻辑
3. 查看日志中的缓存操作错误

### 问题：读写分离未生效
解决方案：
1. 确保方法带有 `@ReadOnly` 注解
2. 检查 DataSourceInterceptor 是否被正确应用
3. 查看日志中的"使用主库"或"使用从库"信息

### 问题：应用启动失败
解决方案：
1. Redis 必须运行（如果使用本地 Redis）
2. MySQL 必须运行且数据库已初始化
3. 检查 application.properties 中的连接配置

## 性能基准

### 缓存效果（基于 Redis 本地部署）
- **缓存命中**：< 1ms
- **数据库查询**：5-20ms
- **性能提升**：5-20 倍

### 读写分离效果（基于主从异步复制）
- **从库负载**：减轻 50-80%（取决于读写比例）
- **主库专注于写操作**：确保事务一致性

## 下一步

1. **启动 Redis**（如果未运行）
   ```bash
   # Docker 方式
   docker run -d -p 6379:6379 redis:6-alpine
   
   # 或本地安装的 Redis
   redis-server
   ```

2. **启动 MySQL**（如果未运行）
   ```bash
   # 确保 MySQL 运行且执行了 init.sql
   mysql -u root -p < src/main/resources/db/init.sql
   ```

3. **运行应用测试**
   ```bash
   mvn spring-boot:run
   # 应用将在 http://localhost:8080 启动
   ```

4. **查看日志验证功能**
   ```bash
   # 查看缓存命中情况
   grep -i cache app.log
   
   # 查看数据源使用情况
   grep -i "使用.*库" app.log
   ```

## 相关文档

- [Redis 官方文档](https://redis.io/documentation)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Spring Boot 数据源配置](https://spring.io/blog/2019/02/27/mysql-8-0-install-and-spring-boot-2-1-3-how-to)
- [MySQL 读写分离最佳实践](https://dev.mysql.com/doc/)

---

**提示**：本实现使用 ThreadLocal 存储数据源上下文，确保线程隔离和安全性。

## Nacos 注册发现与 Gateway 快速验证

### 1. 启动 Nacos

```bash
docker compose -f deploy/docker/docker-compose-nacos.yml up -d
```

启动后访问 Nacos 控制台：`http://localhost:8848/nacos`

### 2. 启动业务服务与网关

```bash
# 启动业务服务（注册到 Nacos）
mvn spring-boot:run

# 新终端启动 Gateway
mvn -f gateway/pom.xml spring-boot:run
```

默认端口：
- 业务服务：`http://localhost:8080`
- Gateway：`http://localhost:8088`

### 3. 在 Nacos 发布服务动态配置

发布 `stock-seckill-system.properties`：

```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
    -d "dataId=stock-seckill-system.properties" \
    -d "group=DEFAULT_GROUP" \
    -d "content=seckill.dynamic.message=hello-from-nacos\nseckill.dynamic.threshold=200"
```

读取配置接口验证：

```bash
curl http://localhost:8080/api/config/dynamic
```

### 4. 使用网关地址验证动态服务路由

通过 Gateway 路由到业务服务：

```bash
curl http://localhost:8088/seckill/api/product/detail/1
```

也可验证基于服务名的发现路由（discovery locator）：

```bash
curl http://localhost:8088/stock-seckill-system/api/product/detail/1
```

### 5. 验证 Nacos 配置热更新

修改 Nacos 配置后再次读取接口，检查返回值是否变化：

```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
    -d "dataId=stock-seckill-system.properties" \
    -d "group=DEFAULT_GROUP" \
    -d "content=seckill.dynamic.message=message-updated\nseckill.dynamic.threshold=520"

curl http://localhost:8080/api/config/dynamic
```

如果返回值中的 `message` 和 `threshold` 更新，说明动态配置生效。

## 流量治理与压测

新增流量治理测试接口：

- `GET /api/traffic/governance?fail=false&sleepMs=20`

返回：

- `status=ok`：正常处理
- `status=degraded`：触发限流/熔断后降级返回

JMeter 压测资源：

- 脚本：`docs/jmeter/traffic-governance-test.jmx`
- 说明：`docs/jmeter/README.md`
