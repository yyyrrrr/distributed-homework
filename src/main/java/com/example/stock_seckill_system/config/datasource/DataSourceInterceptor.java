package com.example.stock_seckill_system.config.datasource;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 读写分离拦截器
 */
@Component
public class DataSourceInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        ReadOnly readOnly = invocation.getMethod().getAnnotation(ReadOnly.class);
        
        if (readOnly == null) {
            // 没有 @ReadOnly 注解，使用主库（默认）
            DataSourceContextHolder.setDataSource(DataSourceContextHolder.MASTER);
            logger.debug("使用主库执行: {}", invocation.getMethod().getName());
        } else if (readOnly.value()) {
            // 有 @ReadOnly 注解且为 true，使用从库
            DataSourceContextHolder.setDataSource(DataSourceContextHolder.SLAVE);
            logger.debug("使用从库执行: {}", invocation.getMethod().getName());
        } else {
            // @ReadOnly(false)，使用主库
            DataSourceContextHolder.setDataSource(DataSourceContextHolder.MASTER);
            logger.debug("使用主库执行: {}", invocation.getMethod().getName());
        }

        try {
            return invocation.proceed();
        } finally {
            // 执行完后清除线程本地变量
            DataSourceContextHolder.clearDataSource();
        }
    }
}
