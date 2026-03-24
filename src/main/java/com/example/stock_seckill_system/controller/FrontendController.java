package com.example.stock_seckill_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端路由控制器
 */
@Controller
public class FrontendController {

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "forward:/pages/index.html";
    }

    /**
     * 商品详情页
     */
    @GetMapping("/detail")
    public String detail() {
        return "forward:/pages/detail.html";
    }
}
