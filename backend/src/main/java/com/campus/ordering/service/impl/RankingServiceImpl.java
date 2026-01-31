package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.Order;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.mapper.DishMapper;
import com.campus.ordering.mapper.OrderMapper;
import com.campus.ordering.mapper.ShopMapper;
import com.campus.ordering.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankingServiceImpl implements RankingService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<Dish> getHotDishes(int limit) {
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getSales)
                .last("LIMIT " + limit));
    }

    @Override
    public List<Shop> getHotShops(int limit) {
        return shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1)
                .orderByDesc(Shop::getMonthlySales)
                .last("LIMIT " + limit));
    }

    @Override
    public List<Dish> getNewDishes(int limit) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .ge(Dish::getCreatedAt, oneWeekAgo)
                .orderByDesc(Dish::getCreatedAt)
                .last("LIMIT " + limit));
    }

    @Override
    public Map<String, Object> getWeeklyRanking() {
        Map<String, Object> result = new HashMap<>();

        // 本周热销菜品
        result.put("hotDishes", getHotDishes(10));

        // 本周热门店铺
        result.put("hotShops", getHotShops(10));

        // 本周新品
        result.put("newDishes", getNewDishes(10));

        // 本周订单统计
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        Long weeklyOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, weekStart)
                .eq(Order::getStatus, 4)); // 已完成订单
        result.put("weeklyOrders", weeklyOrders);

        return result;
    }
}
