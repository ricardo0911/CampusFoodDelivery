package com.campus.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ordering.entity.DishNutrition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishNutritionMapper extends BaseMapper<DishNutrition> {

    @Select("SELECT * FROM dish_nutrition WHERE dish_id = #{dishId}")
    DishNutrition selectByDishId(@Param("dishId") Long dishId);

    @Select("SELECT * FROM dish_nutrition WHERE dish_id IN (${dishIds})")
    List<DishNutrition> selectByDishIds(@Param("dishIds") String dishIds);
}
