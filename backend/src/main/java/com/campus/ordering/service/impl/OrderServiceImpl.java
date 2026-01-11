package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Order;
import com.campus.ordering.mapper.OrderMapper;
import com.campus.ordering.service.OrderService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public List<Order> findByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt));
    }

    @Override
    public List<Order> findByShopId(Long shopId) {
        return this.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getShopId, shopId)
                .orderByDesc(Order::getCreatedAt));
    }

    @Override
    public String generateOrderNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int count = counter.incrementAndGet() % 10000;
        return dateStr + String.format("%04d", count);
    }
}
