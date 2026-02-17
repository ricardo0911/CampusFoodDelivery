package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户画像表
 */
@Data
@TableName(value = "user_profile", autoResultMap = true)
public class UserProfile {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 口味标签 ["辣","清淡","甜"] */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tasteTags;

    /** 菜系偏好权重 {"川菜":0.8,"粤菜":0.3} */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Double> cuisinePreferences;

    /** 过敏信息 ["花生","海鲜"] */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> allergyInfo;

    /** 不喜欢的食材 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> dislikeIngredients;

    /** 平均消费金额 */
    private BigDecimal avgOrderAmount;

    /** 历史订单数 */
    private Integer orderCount;

    /** 常用时段 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> preferredTimeSlots;

    /** 常去店铺ID列表 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> preferredShops;

    /** 健康目标: 1-减脂 2-增肌 3-均衡 */
    private Integer healthGoal;

    /** 每日目标热量(千卡) */
    private Integer dailyCalorieTarget;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
