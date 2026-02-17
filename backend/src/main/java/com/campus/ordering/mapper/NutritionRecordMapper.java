package com.campus.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ordering.entity.NutritionRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface NutritionRecordMapper extends BaseMapper<NutritionRecord> {

    @Select("SELECT * FROM nutrition_record WHERE user_id = #{userId} AND date = #{date}")
    NutritionRecord selectByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT * FROM nutrition_record WHERE user_id = #{userId} AND date >= #{startDate} AND date <= #{endDate} ORDER BY date")
    List<NutritionRecord> selectByUserIdAndDateRange(@Param("userId") Long userId,
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
