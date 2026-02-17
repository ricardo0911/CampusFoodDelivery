package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 推荐日志表
 */
@Data
@TableName(value = "recommendation_log", autoResultMap = true)
public class RecommendationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String sessionId;

    /** 推荐类型: home-首页 search-搜索 cart-购物车 detail-详情页 */
    private String recommendType;

    /** 推荐原因 */
    private String recommendReason;

    /** 推荐的菜品ID列表 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> dishIds;

    /** 各菜品的推荐分数 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<Long, Double> dishScores;

    /** 用户点击的菜品ID */
    private Long clickedDishId;

    /** 最终下单的菜品ID */
    private Long orderedDishId;

    /** 点击时间 */
    private LocalDateTime clickTime;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 场景上下文 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> sceneContext;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 推荐类型常量
    public static final String TYPE_HOME = "home";
    public static final String TYPE_SEARCH = "search";
    public static final String TYPE_CART = "cart";
    public static final String TYPE_DETAIL = "detail";
}
