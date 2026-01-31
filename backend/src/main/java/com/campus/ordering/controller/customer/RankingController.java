package com.campus.ordering.controller.customer;

import com.campus.ordering.service.RankingService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/ranking")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    /**
     * 热销菜品TOP10
     */
    @GetMapping("/hot-dishes")
    public Result<?> hotDishes(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(rankingService.getHotDishes(limit));
    }

    /**
     * 热门店铺排行
     */
    @GetMapping("/hot-shops")
    public Result<?> hotShops(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(rankingService.getHotShops(limit));
    }

    /**
     * 新品推荐
     */
    @GetMapping("/new-dishes")
    public Result<?> newDishes(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(rankingService.getNewDishes(limit));
    }

    /**
     * 本周榜单
     */
    @GetMapping("/weekly")
    public Result<?> weekly() {
        return Result.success(rankingService.getWeeklyRanking());
    }
}
