package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Cart;
import com.campus.ordering.mapper.CartMapper;
import com.campus.ordering.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Override
    public List<Cart> findByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreatedAt));
    }

    @Override
    public void clearByUserId(Long userId) {
        this.remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
    }
}
