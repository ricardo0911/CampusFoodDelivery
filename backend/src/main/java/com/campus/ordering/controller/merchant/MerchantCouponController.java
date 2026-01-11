package com.campus.ordering.controller.merchant;

import com.campus.ordering.entity.Coupon;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.service.CouponService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/coupon")
public class MerchantCouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private ShopService shopService;

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);
        List<Coupon> coupons = couponService.findByShopId(shop.getId());
        return Result.success(coupons);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Coupon coupon, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);
        coupon.setShopId(shop.getId());
        coupon.setRemainCount(coupon.getTotalCount());
        coupon.setStatus(1);
        couponService.save(coupon);
        return Result.success("添加成功", coupon);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);
        Coupon existing = couponService.getById(id);
        if (existing == null || !existing.getShopId().equals(shop.getId())) {
            return Result.error("优惠券不存在");
        }
        couponService.removeById(id);
        return Result.success("删除成功");
    }
}
