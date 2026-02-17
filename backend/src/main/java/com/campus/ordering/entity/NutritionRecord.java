package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 每日营养摄入记录表
 */
@Data
@TableName(value = "nutrition_record", autoResultMap = true)
public class NutritionRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 日期 */
    private LocalDate date;

    /** 总热量(千卡) */
    private Integer totalCalories;

    /** 总蛋白质(克) */
    private BigDecimal totalProtein;

    /** 总碳水(克) */
    private BigDecimal totalCarbs;

    /** 总脂肪(克) */
    private BigDecimal totalFat;

    /** 总膳食纤维(克) */
    private BigDecimal totalFiber;

    /** 总钠(毫克) */
    private Integer totalSodium;

    /** 当日餐数 */
    private Integer mealCount;

    /** 当日订单ID列表 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> orderIds;

    /** 当日热量目标 */
    private Integer calorieGoal;

    /** 是否达标: 0-未达标 1-达标 2-超标 */
    private Integer goalAchieved;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 达标状态常量
    public static final int GOAL_NOT_ACHIEVED = 0;
    public static final int GOAL_ACHIEVED = 1;
    public static final int GOAL_EXCEEDED = 2;
}
