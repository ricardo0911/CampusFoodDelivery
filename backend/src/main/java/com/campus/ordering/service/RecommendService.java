package com.campus.ordering.service;

import com.campus.ordering.dto.RecommendResult;
import com.campus.ordering.entity.Dish;

import java.util.List;
import java.util.Map;

public interface RecommendService {

    /**
     * 首页推荐
     */
    RecommendResult getHomeRecommendations(Long userId, int limit);

    /**
     * 搜索推荐
     */
    RecommendResult getSearchRecommendations(Long userId, String keyword, int limit);

    /**
     * 购物车推荐（根据购物车内容推荐搭配）
     */
    RecommendResult getCartRecommendations(Long userId, List<Long> cartDishIds, int limit);

    /**
     * 详情页推荐（相似菜品）
     */
    RecommendResult getDetailRecommendations(Long userId, Long dishId, int limit);

    /**
     * 记录推荐点击
     */
    void recordClick(Long logId, Long dishId);

    /**
     * 记录推荐下单
     */
    void recordOrder(Long logId, Long dishId);

    /**
     * 计算菜品推荐分数
     */
    Map<Long, Double> calculateDishScores(Long userId, List<Dish> dishes);
}
