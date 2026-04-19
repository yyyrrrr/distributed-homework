package com.example.stock_seckill_system.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TrafficGovernanceService {

    @RateLimiter(name = "traffic-governance", fallbackMethod = "fallback")
    @CircuitBreaker(name = "traffic-governance", fallbackMethod = "fallback")
    public Map<String, Object> guardedCall(boolean fail, long sleepMs) throws InterruptedException {
        if (sleepMs > 0) {
            Thread.sleep(Math.min(sleepMs, 3000));
        }

        if (fail) {
            throw new IllegalStateException("Simulated downstream failure");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("mode", "protected");
        result.put("sleepMs", sleepMs);
        result.put("time", LocalDateTime.now().toString());
        return result;
    }

    public Map<String, Object> fallback(boolean fail, long sleepMs, Throwable throwable) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "degraded");
        result.put("mode", "fallback");
        result.put("sleepMs", sleepMs);
        result.put("fail", fail);
        result.put("reason", throwable.getClass().getSimpleName());
        result.put("message", "触发限流或熔断，已执行降级返回");
        result.put("time", LocalDateTime.now().toString());
        return result;
    }
}
