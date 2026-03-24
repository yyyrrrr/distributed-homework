package com.example.stock_seckill_system.config.datasource;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 数据源上下文，用于切换读写数据源
 */
@Component
public class DataSourceContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static final String MASTER = "master";
    public static final String SLAVE = "slave";

    /**
     * 设置当前数据源
     */
    public static void setDataSource(String dataSourceType) {
        contextHolder.set(dataSourceType);
    }

    /**
     * 获取当前数据源
     */
    public static String getDataSource() {
        String dataSourceType = contextHolder.get();
        if (dataSourceType == null) {
            return MASTER; // 默认使用主库
        }
        return dataSourceType;
    }

    /**
     * 重置数据源
     */
    public static void clearDataSource() {
        contextHolder.remove();
    }
}
