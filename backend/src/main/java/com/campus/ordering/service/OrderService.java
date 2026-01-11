package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {
    List<Order> findByUserId(Long userId);

    List<Order> findByShopId(Long shopId);

    String generateOrderNo();
}
