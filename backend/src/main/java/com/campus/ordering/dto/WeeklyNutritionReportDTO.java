package com.campus.ordering.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 周营养报告DTO
 */
@Data
public class WeeklyNutritionReportDTO {
    /** 开始日期 */
    private String startDate;

    /** 结束日期 */
    private String endDate;

    /** 总摄入热量 */
    private Integer totalCalories;

    /** 日均热量 */
    private Integer avgCalories;

    /** 日均蛋白质 */
    private BigDecimal avgProtein;

    /** 日均碳水 */
    private BigDecimal avgCarbs;

    /** 日均脂肪 */
    private BigDecimal avgFat;

    /** 订餐次数 */
    private Integer orderCount;

    /** 达标天数 */
    private Integer achievedDays;

    /** 超标天数 */
    private Integer exceededDays;

    /** 每日数据 */
    private List<DailyData> dailyData;

    /** 健康评分 (0-100) */
    private Integer healthScore;

    /** 周报总结 */
    private String summary;

    /** 改进建议 */
    private List<String> suggestions;

    @Data
    public static class DailyData {
        private String date;
        private String dayOfWeek;
        private Integer calories;
        private Integer targetCalories;
        private Integer mealCount;
        private Integer status; // 0-未达标 1-达标 2-超标
    }
}
