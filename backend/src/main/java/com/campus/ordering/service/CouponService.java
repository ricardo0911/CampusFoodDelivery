package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Coupon;

import java.util.List;

public interface CouponService extends IService<Coupon> {
    List<Coupon> findByShopId(Long shopId);

    List<Coupon> findAvailable(Long shopId);
}
