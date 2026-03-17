package com.example.stock_seckill_system.service.impl;

import com.example.stock_seckill_system.mapper.UserMapper;
import com.example.stock_seckill_system.model.User;
import com.example.stock_seckill_system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void register(User user) {
        logger.info("用户注册: {}", user.getUsername());
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        logger.info("用户注册成功: {}", user.getUsername());
    }

    @Override
    public User login(String username, String password) {
        logger.info("用户登录: {}", username);
        User user = userMapper.findByUsername(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            logger.info("用户登录成功: {}", username);
            return user;
        }
        logger.info("用户登录失败: {}", username);
        return null;
    }
}