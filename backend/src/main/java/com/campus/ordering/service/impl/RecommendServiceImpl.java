package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.*;
import com.campus.ordering.mapper.*;
import com.campus.ordering.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserPreferenceMapper userPreferenceMapper;

    @Override
    public List<Dish> recommendDishes(Long userId, int limit) {
        // 基于用户历史订单推荐
        if (userId != null) {
            // 获取用户偏好
            List<UserPreference> preferences = userPreferenceMapper.selectList(
                    new LambdaQueryWrapper<UserPreference>()
                            .eq(UserPreference::getUserId, userId)
                            .orderByDesc(UserPreference::getScore)
                            .last("LIMIT 5"));

            if (!preferences.isEmpty()) {
                // 根据偏好推荐相似菜品
                Set<Long> categoryIds = new HashSet<>();
                for (UserPreference pref : preferences) {
                    if ("category".equals(pref.getPreferenceType())) {
                        try {
                            categoryIds.add(Long.parseLong(pref.getPreferenceValue()));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (!categoryIds.isEmpty()) {
                    List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                            .eq(Dish::getStatus, 1)
                            .in(Dish::getCategoryId, categoryIds)
                            .orderByDesc(Dish::getSales)
                            .last("LIMIT " + limit));
                    if (!dishes.isEmpty()) {
                        return dishes;
                    }
                }
            }
        }

        // 默认返回热销菜品
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getSales)
                .last("LIMIT " + limit));
    }

    @Override
    public List<Shop> recommendShops(Long userId, int limit) {
        if (userId != null) {
            // 获取用户常去的店铺
            List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, userId)
                    .eq(Order::getStatus, 4)
                    .orderByDesc(Order::getCreatedAt)
                    .last("LIMIT 20"));

            if (!orders.isEmpty()) {
                // 统计店铺访问频率
                Map<Long, Long> shopFrequency = orders.stream()
                        .collect(Collectors.groupingBy(Order::getShopId, Collectors.counting()));

                // 获取用户没去过但评分高的店铺
                Set<Long> visitedShopIds = shopFrequency.keySet();
                List<Shop> newShops = shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getStatus, 1)
                        .notIn(Shop::getId, visitedShopIds)
                        .orderByDesc(Shop::getRating)
                        .last("LIMIT " + limit));

                if (!newShops.isEmpty()) {
                    return newShops;
                }
            }
        }

        // 默认返回高评分店铺
        return shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1)
                .orderByDesc(Shop::getRating)
                .last("LIMIT " + limit));
    }

    @Override
    public List<Dish> getTodayRecommend(Long userId) {
        // 根据时间段推荐
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String mealType;
        if (hour >= 6 && hour < 10) {
            mealType = "早餐";
        } else if (hour >= 10 && hour < 14) {
            mealType = "午餐";
        } else if (hour >= 14 && hour < 17) {
            mealType = "下午茶";
        } else {
            mealType = "晚餐";
        }

        // 简单实现：返回热销菜品
        return recommendDishes(userId, 6);
    }

    @Override
    public void updatePreference(Long userId, Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) return;

        // 更新分类偏好
        if (dish.getCategoryId() != null) {
            UserPreference pref = userPreferenceMapper.selectOne(
                    new LambdaQueryWrapper<UserPreference>()
                            .eq(UserPreference::getUserId, userId)
                            .eq(UserPreference::getPreferenceType, "category")
                            .eq(UserPreference::getPreferenceValue, dish.getCategoryId().toString()));

            if (pref != null) {
                pref.setScore(pref.getScore() + 1);
                userPreferenceMapper.updateById(pref);
            } else {
                pref = new UserPreference();
                pref.setUserId(userId);
                pref.setPreferenceType("category");
                pref.setPreferenceValue(dish.getCategoryId().toString());
                pref.setScore(1);
                userPreferenceMapper.insert(pref);
            }
        }
    }
}
