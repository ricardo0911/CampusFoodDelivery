package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Review;

import java.util.List;

public interface ReviewService extends IService<Review> {
    List<Review> findByShopId(Long shopId);

    List<Review> findByUserId(Long userId);
}
