package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    List<Dish> findByShopId(Long shopId);

    List<Dish> findByCategoryId(Long categoryId);
}
