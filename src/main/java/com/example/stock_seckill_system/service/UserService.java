package com.example.stock_seckill_system.service;

import com.example.stock_seckill_system.model.User;

public interface UserService {
    void register(User user);
    User login(String username, String password);
}