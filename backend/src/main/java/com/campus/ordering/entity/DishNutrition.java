package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜品营养表
 */
@Data
@TableName(value = "dish_nutrition", autoResultMap = true)
public class DishNutrition {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dishId;

    /** 热量(千卡) */
    private Integer calories;

    /** 蛋白质(克) */
    private BigDecimal protein;

    /** 碳水化合物(克) */
    private BigDecimal carbs;

    /** 脂肪(克) */
    private BigDecimal fat;

    /** 膳食纤维(克) */
    private BigDecimal fiber;

    /** 钠(毫克) */
    private Integer sodium;

    /** 糖(克) */
    private BigDecimal sugar;

    /** 营养标签 ["高蛋白","低脂","素食"] */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /** 主要食材 ["鸡肉","花生","辣椒"] */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> ingredients;

    /** 过敏原 ["花生","大豆"] */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> allergens;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
