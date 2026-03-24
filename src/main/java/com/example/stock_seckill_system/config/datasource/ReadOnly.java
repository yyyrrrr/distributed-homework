package com.example.stock_seckill_system.config.datasource;

import java.lang.annotation.*;

/**
 * 标记使用从库的注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ReadOnly {
    /**
     * 是否使用从库，默认使用
     */
    boolean value() default true;
}
