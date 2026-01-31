package com.campus.ordering.controller.customer;

import com.campus.ordering.service.CheckInService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/check-in")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    /**
     * 签到
     */
    @PostMapping("/sign")
    public Result<?> sign(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = checkInService.sign(userId);

        if ((Boolean) result.get("success")) {
            return Result.success(result.get("message").toString(), result);
        } else {
            return Result.error(result.get("message").toString());
        }
    }

    /**
     * 获取签到状态
     */
    @GetMapping("/status")
    public Result<?> status(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> status = checkInService.getStatus(userId);
        return Result.success(status);
    }

    /**
     * 获取签到历史
     */
    @GetMapping("/history")
    public Result<?> history(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();

        return Result.success(checkInService.findByUserIdAndMonth(userId, year, month));
    }

    /**
     * 获取积分奖励规则
     */
    @GetMapping("/rewards")
    public Result<?> rewards() {
        return Result.success(Map.of(
                "basePoints", 10,
                "day3Bonus", 10,
                "day7Bonus", 20,
                "rules", new String[]{
                        "每日签到可获得10积分",
                        "连续签到3天额外奖励10积分",
                        "连续签到7天额外奖励20积分"
                }
        ));
    }
}
