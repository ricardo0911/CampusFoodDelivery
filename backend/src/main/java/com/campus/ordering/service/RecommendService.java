package com.campus.ordering.service;

import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.Shop;

import java.util.List;

public interface RecommendService {
    List<Dish> recommendDishes(Long userId, int limit);
    List<Shop> recommendShops(Long userId, int limit);
    List<Dish> getTodayRecommend(Long userId);
    void updatePreference(Long userId, Long dishId);
}
