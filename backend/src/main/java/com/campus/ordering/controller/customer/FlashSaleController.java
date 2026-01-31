package com.campus.ordering.controller.customer;

import com.campus.ordering.service.FlashSaleService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/flash-sale")
public class FlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    /**
     * 获取秒杀列表（当前进行中+即将开始）
     */
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(Map.of(
                "current", flashSaleService.getCurrentFlashSales(),
                "upcoming", flashSaleService.getUpcomingFlashSales()
        ));
    }

    /**
     * 当前进行中的秒杀
     */
    @GetMapping("/current")
    public Result<?> current() {
        return Result.success(flashSaleService.getCurrentFlashSales());
    }

    /**
     * 抢购
     */
    @PostMapping("/grab/{id}")
    public Result<?> grab(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = flashSaleService.grab(id, userId);

        if ((Boolean) result.get("success")) {
            return Result.success(result.get("message").toString(), result);
        } else {
            return Result.error(result.get("message").toString());
        }
    }
}
