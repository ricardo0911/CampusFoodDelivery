package com.campus.ordering.controller.admin;

import com.campus.ordering.service.AdminDashboardService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService dashboardService;

    /**
     * 实时数据
     */
    @GetMapping("/realtime")
    public Result<?> realtime() {
        return Result.success(dashboardService.getRealtime());
    }

    /**
     * 今日统计
     */
    @GetMapping("/today-stats")
    public Result<?> todayStats() {
        return Result.success(dashboardService.getTodayStats());
    }

    /**
     * 订单分布
     */
    @GetMapping("/order-map")
    public Result<?> orderMap() {
        return Result.success(dashboardService.getOrderMap());
    }

    /**
     * 趋势数据
     */
    @GetMapping("/trend")
    public Result<?> trend(@RequestParam(defaultValue = "week") String period) {
        return Result.success(dashboardService.getTrend(period));
    }
}
