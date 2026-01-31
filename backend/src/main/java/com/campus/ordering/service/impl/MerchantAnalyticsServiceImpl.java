package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.*;
import com.campus.ordering.mapper.*;
import com.campus.ordering.service.MerchantAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MerchantAnalyticsServiceImpl implements MerchantAnalyticsService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public Map<String, Object> getOverview(Long shopId) {
        Map<String, Object> result = new HashMap<>();

        // 今日数据
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        Long todayOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .ge(Order::getCreatedAt, todayStart)
                .le(Order::getCreatedAt, todayEnd)
                .ne(Order::getStatus, 5)); // 排除已取消

        List<Order> todayOrderList = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .ge(Order::getCreatedAt, todayStart)
                .le(Order::getCreatedAt, todayEnd)
                .eq(Order::getStatus, 4)); // 已完成

        BigDecimal todayRevenue = todayOrderList.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 本月数据
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Order> monthOrderList = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .ge(Order::getCreatedAt, monthStart)
                .eq(Order::getStatus, 4));

        BigDecimal monthRevenue = monthOrderList.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 总订单数
        Long totalOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .eq(Order::getStatus, 4));

        result.put("todayOrders", todayOrders);
        result.put("todayRevenue", todayRevenue);
        result.put("monthOrders", monthOrderList.size());
        result.put("monthRevenue", monthRevenue);
        result.put("totalOrders", totalOrders);

        return result;
    }

    @Override
    public Map<String, Object> getSalesTrend(Long shopId, String period) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();

        LocalDateTime startTime;
        int days;

        switch (period) {
            case "week":
                days = 7;
                break;
            case "month":
                days = 30;
                break;
            default:
                days = 7;
        }

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            List<Order> dayOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .eq(Order::getShopId, shopId)
                    .ge(Order::getCreatedAt, dayStart)
                    .le(Order::getCreatedAt, dayEnd)
                    .eq(Order::getStatus, 4));

            BigDecimal revenue = dayOrders.stream()
                    .map(Order::getPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("orders", dayOrders.size());
            dayData.put("revenue", revenue);
            data.add(dayData);
        }

        result.put("data", data);
        return result;
    }

    @Override
    public Map<String, Object> getHotDishes(Long shopId, int limit) {
        Map<String, Object> result = new HashMap<>();

        List<Dish> dishes = dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getShopId, shopId)
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getSales)
                .last("LIMIT " + limit));

        result.put("dishes", dishes);
        return result;
    }

    @Override
    public Map<String, Object> getCustomerAnalysis(Long shopId) {
        Map<String, Object> result = new HashMap<>();

        // 获取最近30天的订单
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .ge(Order::getCreatedAt, monthStart)
                .eq(Order::getStatus, 4));

        // 统计用户
        Set<Long> uniqueUsers = orders.stream()
                .map(Order::getUserId)
                .collect(Collectors.toSet());

        // 统计复购用户
        Map<Long, Long> userOrderCount = orders.stream()
                .collect(Collectors.groupingBy(Order::getUserId, Collectors.counting()));

        long repeatUsers = userOrderCount.values().stream()
                .filter(count -> count > 1)
                .count();

        result.put("totalCustomers", uniqueUsers.size());
        result.put("repeatCustomers", repeatUsers);
        result.put("repeatRate", uniqueUsers.isEmpty() ? 0 :
                BigDecimal.valueOf(repeatUsers * 100.0 / uniqueUsers.size())
                        .setScale(1, RoundingMode.HALF_UP));

        return result;
    }

    @Override
    public Map<String, Object> getPeakHours(Long shopId) {
        Map<String, Object> result = new HashMap<>();

        // 获取最近7天的订单
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .ge(Order::getCreatedAt, weekStart)
                .eq(Order::getStatus, 4));

        // 按小时统计
        Map<Integer, Long> hourlyCount = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().getHour(),
                        Collectors.counting()));

        List<Map<String, Object>> hourlyData = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Map<String, Object> hourData = new HashMap<>();
            hourData.put("hour", i);
            hourData.put("count", hourlyCount.getOrDefault(i, 0L));
            hourlyData.add(hourData);
        }

        result.put("hourlyData", hourlyData);

        // 找出高峰时段
        int peakHour = hourlyCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(12);
        result.put("peakHour", peakHour);

        return result;
    }
}
