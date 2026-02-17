package com.campus.ordering.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 每日营养报告DTO
 */
@Data
public class NutritionReportDTO {
    /** 日期 */
    private String date;

    /** 已摄入热量 */
    private Integer consumedCalories;

    /** 目标热量 */
    private Integer targetCalories;

    /** 热量完成百分比 */
    private Integer caloriePercentage;

    /** 蛋白质摄入 */
    private BigDecimal protein;

    /** 碳水摄入 */
    private BigDecimal carbs;

    /** 脂肪摄入 */
    private BigDecimal fat;

    /** 膳食纤维摄入 */
    private BigDecimal fiber;

    /** 钠摄入 */
    private Integer sodium;

    /** 当日餐数 */
    private Integer mealCount;

    /** 状态描述，如"还需摄入800千卡"、"今日已达标"、"已超标200千卡" */
    private String statusText;

    /** 状态: 0-未达标 1-达标 2-超标 */
    private Integer status;

    /** 营养建议 */
    private String advice;
}
