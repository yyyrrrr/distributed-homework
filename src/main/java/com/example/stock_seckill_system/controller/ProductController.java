package com.example.stock_seckill_system.controller;

import com.example.stock_seckill_system.model.Product;
import com.example.stock_seckill_system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/detail/{id}")
    public ResponseEntity<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/deduct/{id}")
    public ResponseEntity<String> deductStock(@PathVariable Long id) {
        boolean result = productService.deductStock(id, 1);
        if (result) {
            return ResponseEntity.ok("库存扣减成功");
        } else {
            return ResponseEntity.badRequest().body("库存不足");
        }
    }

    @GetMapping("/seckill/{id}")
    public ResponseEntity<String> seckill(@PathVariable Long id) {
        boolean result = productService.deductSeckillStock(id);
        if (result) {
            return ResponseEntity.ok("秒杀成功");
        } else {
            return ResponseEntity.badRequest().body("秒杀失败，库存不足");
        }
    }
}