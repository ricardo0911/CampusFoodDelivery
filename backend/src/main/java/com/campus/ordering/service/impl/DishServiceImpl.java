package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Dish;
import com.campus.ordering.mapper.DishMapper;
import com.campus.ordering.service.DishService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Override
    public List<Dish> findByShopId(Long shopId) {
        return this.list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getShopId, shopId)
                .eq(Dish::getStatus, 1)
                .eq(Dish::getAuditStatus, 1));
    }

    @Override
    public List<Dish> findByCategoryId(Long categoryId) {
        return this.list(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, 1)
                .eq(Dish::getAuditStatus, 1));
    }
}
