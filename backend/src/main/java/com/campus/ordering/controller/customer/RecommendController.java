package com.campus.ordering.controller.customer;

import com.campus.ordering.service.RecommendService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/customer/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    /**
     * 推荐菜品
     */
    @GetMapping("/dishes")
    public Result<?> dishes(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(recommendService.recommendDishes(userId, limit));
    }

    /**
     * 推荐店铺
     */
    @GetMapping("/shops")
    public Result<?> shops(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(recommendService.recommendShops(userId, limit));
    }

    /**
     * 今日推荐
     */
    @GetMapping("/today")
    public Result<?> today(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(recommendService.getTodayRecommend(userId));
    }
}
