package com.campus.ordering.controller.customer;

import com.campus.ordering.dto.NutritionReportDTO;
import com.campus.ordering.dto.WeeklyNutritionReportDTO;
import com.campus.ordering.entity.UserProfile;
import com.campus.ordering.service.NutritionRecordService;
import com.campus.ordering.service.UserProfileService;
import com.campus.ordering.util.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 营养健康接口 - 顾客端
 */
@RestController
@RequestMapping("/api/customer/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionRecordService nutritionRecordService;
    private final UserProfileService userProfileService;

    /**
     * 获取今日营养报告
     */
    @GetMapping("/today")
    public Result<NutritionReportDTO> getTodayReport(@RequestAttribute("userId") Long userId) {
        return Result.success(nutritionRecordService.getTodayReport(userId));
    }

    /**
     * 获取周营养报告
     */
    @GetMapping("/weekly")
    public Result<WeeklyNutritionReportDTO> getWeeklyReport(@RequestAttribute("userId") Long userId) {
        return Result.success(nutritionRecordService.getWeeklyReport(userId));
    }

    /**
     * 获取健康建议
     */
    @GetMapping("/advice")
    public Result<String> getHealthAdvice(@RequestAttribute("userId") Long userId) {
        return Result.success(nutritionRecordService.getHealthAdvice(userId));
    }

    /**
     * 获取用户画像（口味偏好、健康目标等）
     */
    @GetMapping("/profile")
    public Result<UserProfile> getProfile(@RequestAttribute("userId") Long userId) {
        return Result.success(userProfileService.getOrCreateByUserId(userId));
    }

    /**
     * 更新健康目标
     */
    @PutMapping("/health-goal")
    public Result<Void> updateHealthGoal(
            @RequestAttribute("userId") Long userId,
            @RequestBody HealthGoalRequest request) {
        userProfileService.updateHealthGoal(userId, request.getHealthGoal(), request.getDailyCalorieTarget());
        return Result.success(null);
    }

    /**
     * 添加过敏信息
     */
    @PostMapping("/allergy")
    public Result<Void> addAllergy(
            @RequestAttribute("userId") Long userId,
            @RequestParam String allergen) {
        userProfileService.addAllergyInfo(userId, allergen);
        return Result.success(null);
    }

    /**
     * 移除过敏信息
     */
    @DeleteMapping("/allergy")
    public Result<Void> removeAllergy(
            @RequestAttribute("userId") Long userId,
            @RequestParam String allergen) {
        userProfileService.removeAllergyInfo(userId, allergen);
        return Result.success(null);
    }

    /**
     * 获取常见过敏原列表
     */
    @GetMapping("/allergens")
    public Result<List<String>> getAllergenList() {
        return Result.success(List.of(
                "花生", "大豆", "牛奶", "鸡蛋", "小麦", "麸质",
                "海鲜", "虾", "蟹", "鱼", "贝类", "坚果"));
    }

    @Data
    public static class HealthGoalRequest {
        /** 健康目标: 1-减脂 2-增肌 3-均衡 */
        private Integer healthGoal;
        /** 每日目标热量 */
        private Integer dailyCalorieTarget;
    }
}
