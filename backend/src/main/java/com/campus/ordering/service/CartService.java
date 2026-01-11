package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Cart;

import java.util.List;

public interface CartService extends IService<Cart> {
    List<Cart> findByUserId(Long userId);

    void clearByUserId(Long userId);
}
