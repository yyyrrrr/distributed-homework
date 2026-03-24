package com.example.stock_seckill_system.config.datasource;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源配置，配置主从数据源和动态路由
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String masterUrl;

    @Value("${spring.datasource.slave-url:}")
    private String slaveUrl;

    /**
     * 创建主库数据源
     */
    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(masterUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * 创建从库数据源（如果配置了从库 URL，则使用；否则使用主库地址）
     */
    @Bean(name = "slaveDataSource")
    public DataSource slaveDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(slaveUrl.isEmpty() ? masterUrl : slaveUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * 创建动态路由数据源
     */
    @Bean
    @Primary
    public DataSource dynamicRoutingDataSource() {
        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();
        
        // 设置数据源映射
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceContextHolder.MASTER, masterDataSource());
        dataSourceMap.put(DataSourceContextHolder.SLAVE, slaveDataSource());
        
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(masterDataSource());
        
        return routingDataSource;
    }

    /**
     * 配置 AOP 自动代理，自动为带有 @ReadOnly 注解的方法应用拦截器
     */
    @Bean
    public BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
        BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();
        creator.setBeanNames("*ServiceImpl", "*Service");
        creator.setInterceptorNames("dataSourceInterceptor");
        creator.setProxyTargetClass(true);
        return creator;
    }
}
