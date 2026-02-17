package com.campus.ordering.controller.customer;

import com.campus.ordering.dto.RecommendResult;
import com.campus.ordering.service.RecommendService;
import com.campus.ordering.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐接口 - 顾客端
 */
@RestController
@RequestMapping("/api/customer/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /**
     * 首页推荐
     */
    @GetMapping("/home")
    public Result<RecommendResult> getHomeRecommendations(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(recommendService.getHomeRecommendations(userId, limit));
    }

    /**
     * 搜索推荐
     */
    @GetMapping("/search")
    public Result<RecommendResult> getSearchRecommendations(
            @RequestAttribute("userId") Long userId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") int limit) {
        return Result.success(recommendService.getSearchRecommendations(userId, keyword, limit));
    }

    /**
     * 购物车推荐
     */
    @PostMapping("/cart")
    public Result<RecommendResult> getCartRecommendations(
            @RequestAttribute("userId") Long userId,
            @RequestBody List<Long> cartDishIds,
            @RequestParam(defaultValue = "5") int limit) {
        return Result.success(recommendService.getCartRecommendations(userId, cartDishIds, limit));
    }

    /**
     * 详情页推荐
     */
    @GetMapping("/detail/{dishId}")
    public Result<RecommendResult> getDetailRecommendations(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long dishId,
            @RequestParam(defaultValue = "6") int limit) {
        return Result.success(recommendService.getDetailRecommendations(userId, dishId, limit));
    }

    /**
     * 记录推荐点击
     */
    @PostMapping("/click")
    public Result<Void> recordClick(
            @RequestParam Long logId,
            @RequestParam Long dishId) {
        recommendService.recordClick(logId, dishId);
        return Result.success(null);
    }

    /**
     * 记录推荐下单
     */
    @PostMapping("/order")
    public Result<Void> recordOrder(
            @RequestParam Long logId,
            @RequestParam Long dishId) {
        recommendService.recordOrder(logId, dishId);
        return Result.success(null);
    }
}
