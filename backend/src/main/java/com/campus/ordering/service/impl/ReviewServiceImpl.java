package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Review;
import com.campus.ordering.mapper.ReviewMapper;
import com.campus.ordering.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Override
    public List<Review> findByShopId(Long shopId) {
        return this.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getShopId, shopId)
                .eq(Review::getStatus, 1)
                .orderByDesc(Review::getCreatedAt));
    }

    @Override
    public List<Review> findByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreatedAt));
    }
}
