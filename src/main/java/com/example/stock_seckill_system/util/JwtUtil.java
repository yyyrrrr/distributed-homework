package com.example.stock_seckill_system.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {

    private static final String SECRET_KEY = "stock_seckill_system_secret_key";
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    public static String generateToken(String username) {
        // 暂时返回空字符串，避免JWT依赖问题
        return "";
    }

    public static String getUsernameFromToken(String token) {
        // 暂时返回空字符串，避免JWT依赖问题
        return "";
    }

    public static boolean validateToken(String token) {
        // 暂时返回true，避免JWT依赖问题
        return true;
    }
}