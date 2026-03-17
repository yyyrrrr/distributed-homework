package com.example.stock_seckill_system.mapper;

import com.example.stock_seckill_system.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    int insert(User user);
}