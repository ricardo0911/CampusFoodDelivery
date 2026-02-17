package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.dto.NutritionReportDTO;
import com.campus.ordering.dto.WeeklyNutritionReportDTO;
import com.campus.ordering.entity.NutritionRecord;

import java.time.LocalDate;

public interface NutritionRecordService extends IService<NutritionRecord> {

    /**
     * 获取用户某天的营养记录
     */
    NutritionRecord getByUserIdAndDate(Long userId, LocalDate date);

    /**
     * 根据订单更新当日营养记录
     */
    void updateFromOrder(Long userId, Long orderId);

    /**
     * 获取今日营养报告
     */
    NutritionReportDTO getTodayReport(Long userId);

    /**
     * 获取周报
     */
    WeeklyNutritionReportDTO getWeeklyReport(Long userId);

    /**
     * 获取健康建议
     */
    String getHealthAdvice(Long userId);
}
