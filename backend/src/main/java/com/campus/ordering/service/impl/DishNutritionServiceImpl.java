package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.DishNutrition;
import com.campus.ordering.mapper.DishNutritionMapper;
import com.campus.ordering.service.DishNutritionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishNutritionServiceImpl extends ServiceImpl<DishNutritionMapper, DishNutrition>
        implements DishNutritionService {

    @Override
    public DishNutrition getByDishId(Long dishId) {
        return baseMapper.selectByDishId(dishId);
    }

    @Override
    public Map<Long, DishNutrition> getByDishIds(List<Long> dishIds) {
        if (dishIds == null || dishIds.isEmpty()) {
            return new HashMap<>();
        }

        LambdaQueryWrapper<DishNutrition> query = new LambdaQueryWrapper<>();
        query.in(DishNutrition::getDishId, dishIds);
        List<DishNutrition> list = list(query);

        return list.stream().collect(Collectors.toMap(DishNutrition::getDishId, n -> n));
    }

    @Override
    public boolean saveOrUpdateByDishId(DishNutrition nutrition) {
        DishNutrition existing = getByDishId(nutrition.getDishId());
        if (existing != null) {
            nutrition.setId(existing.getId());
            return updateById(nutrition);
        } else {
            return save(nutrition);
        }
    }
}
