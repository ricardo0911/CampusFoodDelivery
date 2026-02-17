package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.*;
import com.campus.ordering.mapper.UserProfileMapper;
import com.campus.ordering.service.UserProfileService;
import com.campus.ordering.service.OrderService;
import com.campus.ordering.service.OrderItemService;
import com.campus.ordering.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final DishService dishService;

    @Override
    public UserProfile getOrCreateByUserId(Long userId) {
        UserProfile profile = baseMapper.selectByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setTasteTags(new ArrayList<>());
            profile.setCuisinePreferences(new HashMap<>());
            profile.setAllergyInfo(new ArrayList<>());
            profile.setDislikeIngredients(new ArrayList<>());
            profile.setAvgOrderAmount(BigDecimal.ZERO);
            profile.setOrderCount(0);
            profile.setPreferredTimeSlots(new ArrayList<>());
            profile.setPreferredShops(new ArrayList<>());
            profile.setHealthGoal(3); // 默认均衡
            profile.setDailyCalorieTarget(2000);
            save(profile);
        }
        return profile;
    }

    @Override
    @Transactional
    public void updateProfileFromOrder(Long userId, Long orderId) {
        UserProfile profile = getOrCreateByUserId(userId);
        Order order = orderService.getById(orderId);
        if (order == null)
            return;

        // 更新订单统计
        int newOrderCount = profile.getOrderCount() + 1;
        BigDecimal totalAmount = profile.getAvgOrderAmount()
                .multiply(BigDecimal.valueOf(profile.getOrderCount()))
                .add(order.getPayAmount());
        BigDecimal newAvgAmount = totalAmount.divide(BigDecimal.valueOf(newOrderCount), 2, RoundingMode.HALF_UP);

        profile.setOrderCount(newOrderCount);
        profile.setAvgOrderAmount(newAvgAmount);

        // 更新常去店铺
        List<Long> preferredShops = profile.getPreferredShops();
        if (preferredShops == null)
            preferredShops = new ArrayList<>();
        if (!preferredShops.contains(order.getShopId())) {
            preferredShops.add(order.getShopId());
            if (preferredShops.size() > 10)
                preferredShops.remove(0);
        }
        profile.setPreferredShops(preferredShops);

        // 更新口味偏好（基于订单菜品）
        updateTastePreferences(profile, orderId);

        // 更新常用时段
        updatePreferredTimeSlots(profile, order);

        updateById(profile);
    }

    private void updateTastePreferences(UserProfile profile, Long orderId) {
        // 获取订单菜品
        LambdaQueryWrapper<OrderItem> query = new LambdaQueryWrapper<>();
        query.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemService.list(query);

        Map<String, Double> cuisinePrefs = profile.getCuisinePreferences();
        if (cuisinePrefs == null)
            cuisinePrefs = new HashMap<>();

        for (OrderItem item : items) {
            Dish dish = dishService.getById(item.getDishId());
            if (dish != null && dish.getDescription() != null) {
                // 简单的口味标签提取
                String desc = dish.getDescription();
                if (desc.contains("辣") || desc.contains("川")) {
                    cuisinePrefs.merge("川菜", 0.1, Double::sum);
                }
                if (desc.contains("清淡") || desc.contains("粤")) {
                    cuisinePrefs.merge("粤菜", 0.1, Double::sum);
                }
                if (desc.contains("面")) {
                    cuisinePrefs.merge("面食", 0.1, Double::sum);
                }
            }
        }

        // 归一化偏好值（最大为1.0）
        cuisinePrefs.replaceAll((k, v) -> Math.min(v, 1.0));
        profile.setCuisinePreferences(cuisinePrefs);
    }

    private void updatePreferredTimeSlots(UserProfile profile, Order order) {
        List<String> timeSlots = profile.getPreferredTimeSlots();
        if (timeSlots == null)
            timeSlots = new ArrayList<>();

        String hour = String.format("%02d:00", order.getCreatedAt().getHour());
        if (!timeSlots.contains(hour)) {
            timeSlots.add(hour);
            if (timeSlots.size() > 5)
                timeSlots.remove(0);
        }
        profile.setPreferredTimeSlots(timeSlots);
    }

    @Override
    @Transactional
    public void updateHealthGoal(Long userId, Integer healthGoal, Integer dailyCalorieTarget) {
        UserProfile profile = getOrCreateByUserId(userId);
        profile.setHealthGoal(healthGoal);
        profile.setDailyCalorieTarget(dailyCalorieTarget);
        updateById(profile);
    }

    @Override
    @Transactional
    public void addAllergyInfo(Long userId, String allergen) {
        UserProfile profile = getOrCreateByUserId(userId);
        List<String> allergies = profile.getAllergyInfo();
        if (allergies == null)
            allergies = new ArrayList<>();
        if (!allergies.contains(allergen)) {
            allergies.add(allergen);
            profile.setAllergyInfo(allergies);
            updateById(profile);
        }
    }

    @Override
    @Transactional
    public void removeAllergyInfo(Long userId, String allergen) {
        UserProfile profile = getOrCreateByUserId(userId);
        List<String> allergies = profile.getAllergyInfo();
        if (allergies != null && allergies.remove(allergen)) {
            profile.setAllergyInfo(allergies);
            updateById(profile);
        }
    }
}
