package com.campus.ordering.service;

import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.Shop;

import java.util.List;
import java.util.Map;

public interface RankingService {
    List<Dish> getHotDishes(int limit);
    List<Shop> getHotShops(int limit);
    List<Dish> getNewDishes(int limit);
    Map<String, Object> getWeeklyRanking();
}
