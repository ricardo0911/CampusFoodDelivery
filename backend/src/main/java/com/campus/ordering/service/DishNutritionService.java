package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.DishNutrition;

import java.util.List;
import java.util.Map;

public interface DishNutritionService extends IService<DishNutrition> {

    /**
     * 根据菜品ID获取营养信息
     */
    DishNutrition getByDishId(Long dishId);

    /**
     * 批量获取菜品营养信息
     */
    Map<Long, DishNutrition> getByDishIds(List<Long> dishIds);

    /**
     * 保存或更新菜品营养信息
     */
    boolean saveOrUpdateByDishId(DishNutrition nutrition);
}
