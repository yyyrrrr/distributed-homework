package com.example.stock_seckill_system.controller;

import com.example.stock_seckill_system.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seckill")
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    @PostMapping("/do")
    public String doSeckill(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            seckillService.seckill(userId, productId);
            return "秒杀成功，订单正在处理中";
        } catch (Exception e) {
            return "秒杀失败：" + e.getMessage();
        }
    }
}
