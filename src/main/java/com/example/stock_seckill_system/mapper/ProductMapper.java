package com.example.stock_seckill_system.mapper;

import com.example.stock_seckill_system.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {
    Product findById(@Param("id") Long id);
    List<Product> selectAll();
    int updateStock(@Param("id") Long id);
    int updateStockByQuantity(@Param("id") Long id, @Param("stock") Integer stock);
    int updateSeckillStock(@Param("id") Long id, @Param("seckillStock") Integer seckillStock);
}