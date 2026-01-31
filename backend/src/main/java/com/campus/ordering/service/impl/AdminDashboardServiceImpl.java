package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.*;
import com.campus.ordering.mapper.*;
import com.campus.ordering.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public Map<String, Object> getRealtime() {
        Map<String, Object> result = new HashMap<>();

        // 今日实时数据
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 今日订单数
        Long todayOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart)
                .ne(Order::getStatus, 5));

        // 今日营业额
        List<Order> completedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart)
                .eq(Order::getStatus, 4));

        BigDecimal todayRevenue = completedOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 今日新用户
        Long todayNewUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreatedAt, todayStart));

        // 待处理订单
        Long pendingOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 1));

        result.put("todayOrders", todayOrders);
        result.put("todayRevenue", todayRevenue);
        result.put("todayNewUsers", todayNewUsers);
        result.put("pendingOrders", pendingOrders);

        return result;
    }

    @Override
    public Map<String, Object> getTodayStats() {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        // 各状态订单统计
        Map<String, Long> orderStats = new HashMap<>();
        orderStats.put("pending", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart).eq(Order::getStatus, 0)));
        orderStats.put("accepted", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart).eq(Order::getStatus, 1)));
        orderStats.put("preparing", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart).eq(Order::getStatus, 2)));
        orderStats.put("delivering", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart).eq(Order::getStatus, 3)));
        orderStats.put("completed", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart).eq(Order::getStatus, 4)));
        orderStats.put("cancelled", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart).eq(Order::getStatus, 5)));

        result.put("orderStats", orderStats);

        // 总计数据
        result.put("totalUsers", userMapper.selectCount(null));
        result.put("totalMerchants", merchantMapper.selectCount(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getStatus, 1)));
        result.put("totalShops", shopMapper.selectCount(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1)));
        result.put("totalOrders", orderMapper.selectCount(null));

        return result;
    }

    @Override
    public Map<String, Object> getOrderMap() {
        Map<String, Object> result = new HashMap<>();

        // 按店铺统计订单分布
        List<Order> recentOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, LocalDateTime.now().minusDays(7))
                .eq(Order::getStatus, 4));

        Map<Long, Long> shopOrderCount = new HashMap<>();
        for (Order order : recentOrders) {
            shopOrderCount.merge(order.getShopId(), 1L, Long::sum);
        }

        List<Map<String, Object>> shopData = new ArrayList<>();
        for (Map.Entry<Long, Long> entry : shopOrderCount.entrySet()) {
            Shop shop = shopMapper.selectById(entry.getKey());
            if (shop != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("shopId", entry.getKey());
                data.put("shopName", shop.getName());
                data.put("orderCount", entry.getValue());
                shopData.add(data);
            }
        }

        // 按订单数排序
        shopData.sort((a, b) -> Long.compare((Long) b.get("orderCount"), (Long) a.get("orderCount")));

        result.put("shopDistribution", shopData);
        return result;
    }

    @Override
    public Map<String, Object> getTrend(String period) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();

        int days = "month".equals(period) ? 30 : 7;

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            Long orderCount = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreatedAt, dayStart)
                    .le(Order::getCreatedAt, dayEnd)
                    .eq(Order::getStatus, 4));

            List<Order> dayOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreatedAt, dayStart)
                    .le(Order::getCreatedAt, dayEnd)
                    .eq(Order::getStatus, 4));

            BigDecimal revenue = dayOrders.stream()
                    .map(Order::getPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Long newUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                    .ge(User::getCreatedAt, dayStart)
                    .le(User::getCreatedAt, dayEnd));

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("orders", orderCount);
            dayData.put("revenue", revenue);
            dayData.put("newUsers", newUsers);
            data.add(dayData);
        }

        result.put("data", data);
        return result;
    }
}
