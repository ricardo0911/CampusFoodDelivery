package com.campus.ordering.controller.merchant;

import com.campus.ordering.service.MerchantAnalyticsService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/merchant/analytics")
public class MerchantAnalyticsController {

    @Autowired
    private MerchantAnalyticsService analyticsService;

    @Autowired
    private ShopService shopService;

    /**
     * 总览数据
     */
    @GetMapping("/overview")
    public Result<?> overview(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();
        return Result.success(analyticsService.getOverview(shopId));
    }

    /**
     * 销售趋势
     */
    @GetMapping("/sales-trend")
    public Result<?> salesTrend(
            @RequestParam(defaultValue = "week") String period,
            HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();
        return Result.success(analyticsService.getSalesTrend(shopId, period));
    }

    /**
     * 热销菜品分析
     */
    @GetMapping("/hot-dishes")
    public Result<?> hotDishes(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();
        return Result.success(analyticsService.getHotDishes(shopId, limit));
    }

    /**
     * 顾客分析
     */
    @GetMapping("/customer-analysis")
    public Result<?> customerAnalysis(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();
        return Result.success(analyticsService.getCustomerAnalysis(shopId));
    }

    /**
     * 高峰时段分析
     */
    @GetMapping("/peak-hours")
    public Result<?> peakHours(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();
        return Result.success(analyticsService.getPeakHours(shopId));
    }
}
