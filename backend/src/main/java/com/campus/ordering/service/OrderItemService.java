package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.OrderItem;

import java.util.List;

public interface OrderItemService extends IService<OrderItem> {
    List<OrderItem> findByOrderId(Long orderId);
}
