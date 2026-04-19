# 商品库存与秒杀系统

## 项目简介

本项目是一个基于Spring Boot的商品库存与秒杀系统，实现了用户注册登录、商品管理、订单管理和库存管理等核心功能，并针对秒杀场景进行了特殊优化，确保系统在高并发情况下的稳定性和可靠性。

## 技术栈

- **编程语言**：Java 17
- **框架**：Spring Boot 3.2.5
- **微服务组件**：Spring Cloud Gateway、Nacos（注册中心 + 配置中心）
- **持久层**：MyBatis 4.0.1
- **数据库**：MySQL 8.0+
- **安全**：Spring Security
- **构建工具**：Maven

## 项目结构

```
distributed-homework/
├── src/
│   ├── main/
│   │   ├── java/com/example/stock_seckill_system/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/       # 控制器
│   │   │   ├── exception/        # 异常处理
│   │   │   ├── mapper/           # MyBatis映射
│   │   │   ├── model/            # 数据模型
│   │   │   ├── service/          # 业务逻辑
│   │   │   ├── util/             # 工具类
│   │   │   └── StockSeckillSystemApplication.java  # 应用入口
│   │   └── resources/
│   │       ├── db/               # 数据库脚本
│   │       ├── mappers/          # MyBatis XML映射
│   │       └── application.properties  # 配置文件
│   └── test/                     # 测试代码
├── gateway/                      # Spring Cloud Gateway 子工程
│   ├── src/main/java             # 网关启动类
│   └── src/main/resources        # 网关路由与Nacos配置
├── pom.xml                       # Maven依赖
├── 系统设计文档.md                 # 系统设计文档
└── README.md                     # 项目说明
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

1. 登录MySQL，创建数据库：
   ```sql
   CREATE DATABASE IF NOT EXISTS stock_seckill_system;
   ```

2. 执行初始化脚本：
   ```bash
   mysql -u root -p stock_seckill_system < src/main/resources/db/init.sql
   ```

### 3. 配置修改

修改 `src/main/resources/application.properties` 文件中的数据库连接信息：

```properties
spring.datasource.username=你的数据库用户名
spring.datasource.password=你的数据库密码
```

### 4. 构建项目

```bash
mvn clean compile
```

### 5. 运行项目

```bash
mvn spring-boot:run
```

项目将在 `http://localhost:8080` 启动。

## 功能模块

### 1. 用户模块
- 用户注册
- 用户登录
- 用户信息管理

### 2. 商品模块
- 商品列表
- 商品详情
- 商品管理

### 3. 订单模块
- 订单创建
- 订单查询
- 订单状态管理

### 4. 库存模块
- 库存查询
- 库存扣减
- 库存管理

## API接口

### 用户接口
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/update` - 更新用户信息

### 商品接口
- `GET /api/product/list` - 获取商品列表
- `GET /api/product/detail/{id}` - 获取商品详情
- `POST /api/product/create` - 创建商品
- `PUT /api/product/update/{id}` - 更新商品

### 订单接口
- `POST /api/order/create` - 创建订单
- `GET /api/order/list` - 获取用户订单列表
- `GET /api/order/detail/{id}` - 获取订单详情
- `PUT /api/order/cancel/{id}` - 取消订单

### 库存接口
- `GET /api/stock/check/{productId}` - 检查库存
- `POST /api/stock/deduct` - 扣减库存
- `POST /api/stock/add` - 增加库存

## 系统设计

详细的系统设计文档请参考 [系统设计文档.md](系统设计文档.md)，其中包含：

- 系统架构设计
- 服务拆分说明
- API接口设计
- 数据库设计
- 技术栈选型
- 系统安全性
- 性能优化
- 监控与告警