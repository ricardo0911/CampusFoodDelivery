package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.Order;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.mapper.OrderMapper;
import com.campus.ordering.mapper.ShopMapper;
import com.campus.ordering.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FinanceServiceImpl implements FinanceService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShopMapper shopMapper;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05"); // 5%佣金

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();

        // 总营业额
        List<Order> allCompletedOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 4));

        BigDecimal totalRevenue = allCompletedOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 总佣金
        BigDecimal totalCommission = totalRevenue.multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // 本月数据
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Order> monthOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, monthStart)
                .eq(Order::getStatus, 4));

        BigDecimal monthRevenue = monthOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthCommission = monthRevenue.multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        // 今日数据
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        List<Order> todayOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, todayStart)
                .eq(Order::getStatus, 4));

        BigDecimal todayRevenue = todayOrders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("totalRevenue", totalRevenue);
        result.put("totalCommission", totalCommission);
        result.put("monthRevenue", monthRevenue);
        result.put("monthCommission", monthCommission);
        result.put("todayRevenue", todayRevenue);
        result.put("todayOrders", todayOrders.size());

        return result;
    }

    @Override
    public Map<String, Object> getCommission(String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, start)
                .le(Order::getCreatedAt, end)
                .eq(Order::getStatus, 4));

        // 按店铺统计
        Map<Long, BigDecimal> shopRevenue = new HashMap<>();
        for (Order order : orders) {
            shopRevenue.merge(order.getShopId(), order.getPayAmount(), BigDecimal::add);
        }

        List<Map<String, Object>> shopData = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : shopRevenue.entrySet()) {
            Shop shop = shopMapper.selectById(entry.getKey());
            if (shop != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("shopId", entry.getKey());
                data.put("shopName", shop.getName());
                data.put("revenue", entry.getValue());
                data.put("commission", entry.getValue().multiply(COMMISSION_RATE)
                        .setScale(2, RoundingMode.HALF_UP));
                shopData.add(data);
            }
        }

        // 按佣金排序
        shopData.sort((a, b) -> ((BigDecimal) b.get("commission")).compareTo((BigDecimal) a.get("commission")));

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.put("shopData", shopData);
        result.put("totalRevenue", totalRevenue);
        result.put("totalCommission", totalRevenue.multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    @Override
    public Map<String, Object> getSettlement(Long shopId, String month) {
        Map<String, Object> result = new HashMap<>();

        LocalDate monthDate = LocalDate.parse(month + "-01");
        LocalDateTime start = monthDate.atStartOfDay();
        LocalDateTime end = monthDate.plusMonths(1).minusDays(1).atTime(LocalTime.MAX);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, start)
                .le(Order::getCreatedAt, end)
                .eq(Order::getStatus, 4);

        if (shopId != null) {
            wrapper.eq(Order::getShopId, shopId);
        }

        List<Order> orders = orderMapper.selectList(wrapper);

        BigDecimal totalRevenue = orders.stream()
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal commission = totalRevenue.multiply(COMMISSION_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal settlement = totalRevenue.subtract(commission);

        result.put("month", month);
        result.put("orderCount", orders.size());
        result.put("totalRevenue", totalRevenue);
        result.put("commission", commission);
        result.put("settlement", settlement);

        return result;
    }

    @Override
    public byte[] exportReport(String type, String startDate, String endDate) {
        // 简化实现：返回CSV格式数据
        StringBuilder sb = new StringBuilder();
        sb.append("日期,订单数,营业额,佣金\n");

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            List<Order> dayOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                    .ge(Order::getCreatedAt, dayStart)
                    .le(Order::getCreatedAt, dayEnd)
                    .eq(Order::getStatus, 4));

            BigDecimal revenue = dayOrders.stream()
                    .map(Order::getPayAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal commission = revenue.multiply(COMMISSION_RATE)
                    .setScale(2, RoundingMode.HALF_UP);

            sb.append(date.toString()).append(",")
                    .append(dayOrders.size()).append(",")
                    .append(revenue).append(",")
                    .append(commission).append("\n");
        }

        return sb.toString().getBytes();
    }
}
