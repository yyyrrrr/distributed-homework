package com.example.stock_seckill_system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RefreshScope
public class NacosConfigController {

    @Value("${seckill.dynamic.message:default-message}")
    private String message;

    @Value("${seckill.dynamic.threshold:100}")
    private Integer threshold;

    @GetMapping("/dynamic")
    public Map<String, Object> getDynamicConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", message);
        result.put("threshold", threshold);
        result.put("app", "stock-seckill-system");
        result.put("readAt", LocalDateTime.now().toString());
        return result;
    }
}
