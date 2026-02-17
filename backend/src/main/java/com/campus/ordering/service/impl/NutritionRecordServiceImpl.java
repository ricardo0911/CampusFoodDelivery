package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.dto.NutritionReportDTO;
import com.campus.ordering.dto.WeeklyNutritionReportDTO;
import com.campus.ordering.entity.*;
import com.campus.ordering.mapper.NutritionRecordMapper;
import com.campus.ordering.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NutritionRecordServiceImpl extends ServiceImpl<NutritionRecordMapper, NutritionRecord>
        implements NutritionRecordService {

    private final UserProfileService userProfileService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final DishNutritionService nutritionService;

    @Override
    public NutritionRecord getByUserIdAndDate(Long userId, LocalDate date) {
        return baseMapper.selectByUserIdAndDate(userId, date);
    }

    @Override
    @Transactional
    public void updateFromOrder(Long userId, Long orderId) {
        LocalDate today = LocalDate.now();
        NutritionRecord record = getByUserIdAndDate(userId, today);

        if (record == null) {
            UserProfile profile = userProfileService.getOrCreateByUserId(userId);
            record = new NutritionRecord();
            record.setUserId(userId);
            record.setDate(today);
            record.setTotalCalories(0);
            record.setTotalProtein(BigDecimal.ZERO);
            record.setTotalCarbs(BigDecimal.ZERO);
            record.setTotalFat(BigDecimal.ZERO);
            record.setTotalFiber(BigDecimal.ZERO);
            record.setTotalSodium(0);
            record.setMealCount(0);
            record.setOrderIds(new ArrayList<>());
            record.setCalorieGoal(profile.getDailyCalorieTarget() != null ? profile.getDailyCalorieTarget() : 2000);
        }

        // 获取订单菜品
        LambdaQueryWrapper<OrderItem> query = new LambdaQueryWrapper<>();
        query.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemService.list(query);

        List<Long> dishIds = items.stream().map(OrderItem::getDishId).collect(Collectors.toList());
        Map<Long, DishNutrition> nutritions = nutritionService.getByDishIds(dishIds);

        // 累加营养数据
        for (OrderItem item : items) {
            DishNutrition nutrition = nutritions.get(item.getDishId());
            if (nutrition != null) {
                int quantity = item.getQuantity() != null ? item.getQuantity() : 1;

                record.setTotalCalories(record.getTotalCalories() + nutrition.getCalories() * quantity);
                record.setTotalProtein(safeAdd(record.getTotalProtein(), nutrition.getProtein(), quantity));
                record.setTotalCarbs(safeAdd(record.getTotalCarbs(), nutrition.getCarbs(), quantity));
                record.setTotalFat(safeAdd(record.getTotalFat(), nutrition.getFat(), quantity));
                record.setTotalFiber(safeAdd(record.getTotalFiber(), nutrition.getFiber(), quantity));
                record.setTotalSodium(record.getTotalSodium()
                        + (nutrition.getSodium() != null ? nutrition.getSodium() : 0) * quantity);
            }
        }

        // 更新餐数和订单列表
        record.setMealCount(record.getMealCount() + 1);
        List<Long> orderIds = record.getOrderIds();
        if (orderIds == null)
            orderIds = new ArrayList<>();
        orderIds.add(orderId);
        record.setOrderIds(orderIds);

        // 更新达标状态
        updateGoalStatus(record);

        saveOrUpdate(record);
    }

    private BigDecimal safeAdd(BigDecimal base, BigDecimal add, int quantity) {
        if (add == null)
            return base;
        return base.add(add.multiply(BigDecimal.valueOf(quantity)));
    }

    private void updateGoalStatus(NutritionRecord record) {
        int calories = record.getTotalCalories();
        int goal = record.getCalorieGoal();

        if (calories < goal * 0.8) {
            record.setGoalAchieved(NutritionRecord.GOAL_NOT_ACHIEVED);
        } else if (calories <= goal * 1.1) {
            record.setGoalAchieved(NutritionRecord.GOAL_ACHIEVED);
        } else {
            record.setGoalAchieved(NutritionRecord.GOAL_EXCEEDED);
        }
    }

    @Override
    public NutritionReportDTO getTodayReport(Long userId) {
        LocalDate today = LocalDate.now();
        NutritionRecord record = getByUserIdAndDate(userId, today);
        UserProfile profile = userProfileService.getOrCreateByUserId(userId);

        NutritionReportDTO dto = new NutritionReportDTO();
        dto.setDate(today.toString());
        dto.setTargetCalories(profile.getDailyCalorieTarget() != null ? profile.getDailyCalorieTarget() : 2000);

        if (record != null) {
            dto.setConsumedCalories(record.getTotalCalories());
            dto.setProtein(record.getTotalProtein());
            dto.setCarbs(record.getTotalCarbs());
            dto.setFat(record.getTotalFat());
            dto.setFiber(record.getTotalFiber());
            dto.setSodium(record.getTotalSodium());
            dto.setMealCount(record.getMealCount());
            dto.setStatus(record.getGoalAchieved());

            int percentage = (int) (record.getTotalCalories() * 100.0 / dto.getTargetCalories());
            dto.setCaloriePercentage(percentage);

            int diff = dto.getTargetCalories() - record.getTotalCalories();
            if (diff > 0) {
                dto.setStatusText("还需摄入 " + diff + " 千卡");
            } else if (diff < -dto.getTargetCalories() * 0.1) {
                dto.setStatusText("已超标 " + (-diff) + " 千卡");
            } else {
                dto.setStatusText("今日已达标 ✓");
            }
        } else {
            dto.setConsumedCalories(0);
            dto.setProtein(BigDecimal.ZERO);
            dto.setCarbs(BigDecimal.ZERO);
            dto.setFat(BigDecimal.ZERO);
            dto.setFiber(BigDecimal.ZERO);
            dto.setSodium(0);
            dto.setMealCount(0);
            dto.setCaloriePercentage(0);
            dto.setStatus(0);
            dto.setStatusText("今日还未进餐");
        }

        dto.setAdvice(generateAdvice(dto, profile));
        return dto;
    }

    private String generateAdvice(NutritionReportDTO dto, UserProfile profile) {
        if (dto.getMealCount() == 0) {
            return "记得按时吃饭哦～";
        }

        StringBuilder advice = new StringBuilder();

        // 根据健康目标给建议
        if (profile.getHealthGoal() != null) {
            switch (profile.getHealthGoal()) {
                case 1: // 减脂
                    if (dto.getCaloriePercentage() > 90) {
                        advice.append("今日热量接近上限，建议选择低脂餐品。");
                    }
                    break;
                case 2: // 增肌
                    if (dto.getProtein() != null && dto.getProtein().doubleValue() < 50) {
                        advice.append("蛋白质摄入不足，建议增加高蛋白食物。");
                    }
                    break;
            }
        }

        if (advice.length() == 0) {
            advice.append("继续保持均衡饮食！");
        }

        return advice.toString();
    }

    @Override
    public WeeklyNutritionReportDTO getWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today;

        List<NutritionRecord> records = baseMapper.selectByUserIdAndDateRange(userId, startOfWeek, endOfWeek);
        UserProfile profile = userProfileService.getOrCreateByUserId(userId);

        WeeklyNutritionReportDTO dto = new WeeklyNutritionReportDTO();
        dto.setStartDate(startOfWeek.format(DateTimeFormatter.ofPattern("MM-dd")));
        dto.setEndDate(endOfWeek.format(DateTimeFormatter.ofPattern("MM-dd")));

        int totalCalories = 0;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        int orderCount = 0;
        int achievedDays = 0;
        int exceededDays = 0;

        List<WeeklyNutritionReportDTO.DailyData> dailyData = new ArrayList<>();

        for (int i = 0; i <= today.getDayOfWeek().getValue() - 1; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            WeeklyNutritionReportDTO.DailyData daily = new WeeklyNutritionReportDTO.DailyData();
            daily.setDate(date.format(DateTimeFormatter.ofPattern("MM-dd")));
            daily.setDayOfWeek(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.CHINA));
            daily.setTargetCalories(profile.getDailyCalorieTarget());

            NutritionRecord record = records.stream()
                    .filter(r -> r.getDate().equals(date))
                    .findFirst()
                    .orElse(null);

            if (record != null) {
                daily.setCalories(record.getTotalCalories());
                daily.setMealCount(record.getMealCount());
                daily.setStatus(record.getGoalAchieved());

                totalCalories += record.getTotalCalories();
                totalProtein = totalProtein
                        .add(record.getTotalProtein() != null ? record.getTotalProtein() : BigDecimal.ZERO);
                totalCarbs = totalCarbs.add(record.getTotalCarbs() != null ? record.getTotalCarbs() : BigDecimal.ZERO);
                totalFat = totalFat.add(record.getTotalFat() != null ? record.getTotalFat() : BigDecimal.ZERO);
                orderCount += record.getMealCount();

                if (record.getGoalAchieved() == 1)
                    achievedDays++;
                else if (record.getGoalAchieved() == 2)
                    exceededDays++;
            } else {
                daily.setCalories(0);
                daily.setMealCount(0);
                daily.setStatus(0);
            }

            dailyData.add(daily);
        }

        dto.setDailyData(dailyData);
        dto.setTotalCalories(totalCalories);
        dto.setOrderCount(orderCount);
        dto.setAchievedDays(achievedDays);
        dto.setExceededDays(exceededDays);

        int daysWithData = (int) records.stream().filter(r -> r.getMealCount() > 0).count();
        if (daysWithData > 0) {
            dto.setAvgCalories(totalCalories / daysWithData);
            dto.setAvgProtein(totalProtein.divide(BigDecimal.valueOf(daysWithData), 1, RoundingMode.HALF_UP));
            dto.setAvgCarbs(totalCarbs.divide(BigDecimal.valueOf(daysWithData), 1, RoundingMode.HALF_UP));
            dto.setAvgFat(totalFat.divide(BigDecimal.valueOf(daysWithData), 1, RoundingMode.HALF_UP));
        }

        // 计算健康评分
        int healthScore = calculateHealthScore(achievedDays, exceededDays, daysWithData);
        dto.setHealthScore(healthScore);

        // 生成总结
        dto.setSummary(generateWeeklySummary(dto, daysWithData));
        dto.setSuggestions(generateSuggestions(dto, profile));

        return dto;
    }

    private int calculateHealthScore(int achieved, int exceeded, int total) {
        if (total == 0)
            return 50;
        return (int) ((achieved * 100.0 / total) - (exceeded * 20.0 / total));
    }

    private String generateWeeklySummary(WeeklyNutritionReportDTO dto, int daysWithData) {
        if (daysWithData == 0) {
            return "本周还没有订餐记录";
        }
        return String.format("本周共订餐 %d 次，%d 天达标，日均摄入 %d 千卡",
                dto.getOrderCount(), dto.getAchievedDays(), dto.getAvgCalories());
    }

    private List<String> generateSuggestions(WeeklyNutritionReportDTO dto, UserProfile profile) {
        List<String> suggestions = new ArrayList<>();

        if (dto.getExceededDays() > 2) {
            suggestions.add("本周有多天热量超标，建议控制每餐份量");
        }
        if (dto.getAvgProtein() != null && dto.getAvgProtein().doubleValue() < 40) {
            suggestions.add("蛋白质摄入偏低，可以多选择鸡肉、鱼类等高蛋白食物");
        }
        if (dto.getOrderCount() < 3) {
            suggestions.add("本周订餐较少，记得按时吃饭哦");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("继续保持良好的饮食习惯！");
        }

        return suggestions;
    }

    @Override
    public String getHealthAdvice(Long userId) {
        NutritionReportDTO today = getTodayReport(userId);
        return today.getAdvice();
    }
}
