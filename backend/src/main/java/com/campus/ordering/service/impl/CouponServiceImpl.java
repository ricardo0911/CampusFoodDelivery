package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Coupon;
import com.campus.ordering.mapper.CouponMapper;
import com.campus.ordering.service.CouponService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Override
    public List<Coupon> findByShopId(Long shopId) {
        return this.list(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getShopId, shopId)
                .orderByDesc(Coupon::getCreatedAt));
    }

    @Override
    public List<Coupon> findAvailable(Long shopId) {
        LocalDateTime now = LocalDateTime.now();
        return this.list(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getShopId, shopId)
                .eq(Coupon::getStatus, 1)
                .le(Coupon::getStartTime, now)
                .ge(Coupon::getEndTime, now)
                .gt(Coupon::getRemainCount, 0));
    }
}
