package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.DishCategory;

import java.util.List;

public interface DishCategoryService extends IService<DishCategory> {
    List<DishCategory> findByShopId(Long shopId);
}
