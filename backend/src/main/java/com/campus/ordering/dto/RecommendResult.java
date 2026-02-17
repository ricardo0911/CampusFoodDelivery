package com.campus.ordering.dto;

import com.campus.ordering.entity.Dish;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 推荐结果DTO
 */
@Data
public class RecommendResult {
    /** 推荐日志ID，用于追踪效果 */
    private Long logId;

    /** 推荐类型 */
    private String type;

    /** 推荐原因 */
    private String reason;

    /** 推荐的菜品列表 */
    private List<RecommendedDish> dishes;

    @Data
    public static class RecommendedDish {
        private Long id;
        private String name;
        private String image;
        private String description;
        private Double price;
        private Integer sales;
        private Double score;
        private String shopName;
        private Long shopId;
        /** 推荐标签，如"你常点"、"高蛋白"、"热销" */
        private List<String> recommendTags;
        /** 营养信息摘要 */
        private NutritionSummary nutrition;
    }

    @Data
    public static class NutritionSummary {
        private Integer calories;
        private String tags; // "低脂 高蛋白"
    }
}
