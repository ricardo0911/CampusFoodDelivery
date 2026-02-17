package com.campus.ordering.controller.merchant;

import com.campus.ordering.entity.DishNutrition;
import com.campus.ordering.entity.Reservation;
import com.campus.ordering.entity.ShopTimeslotConfig;
import com.campus.ordering.mapper.ShopTimeslotConfigMapper;
import com.campus.ordering.service.DishNutritionService;
import com.campus.ordering.service.PickupTimeslotService;
import com.campus.ordering.service.ReservationService;
import com.campus.ordering.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 商家预约管理接口
 */
@RestController
@RequestMapping("/api/merchant/reservation")
@RequiredArgsConstructor
public class MerchantReservationController {

    private final ReservationService reservationService;
    private final PickupTimeslotService timeslotService;
    private final DishNutritionService nutritionService;
    private final ShopTimeslotConfigMapper configMapper;

    /**
     * 验证取餐码
     */
    @GetMapping("/verify/{pickupCode}")
    public Result<Reservation> verifyPickupCode(@PathVariable String pickupCode) {
        Reservation reservation = reservationService.verifyPickupCode(pickupCode, LocalDate.now());
        if (reservation == null) {
            return Result.error("取餐码无效或已过期");
        }
        return Result.success(reservation);
    }

    /**
     * 标记订单制作中
     */
    @PutMapping("/cooking/{orderId}")
    public Result<Void> markAsCooking(@PathVariable Long orderId) {
        if (reservationService.markAsCooking(orderId)) {
            return Result.success(null);
        }
        return Result.error("操作失败");
    }

    /**
     * 标记订单完成待取
     */
    @PutMapping("/ready/{orderId}")
    public Result<Void> markAsReady(@PathVariable Long orderId) {
        if (reservationService.markAsReady(orderId)) {
            return Result.success(null);
        }
        return Result.error("操作失败");
    }

    /**
     * 确认取餐
     */
    @PutMapping("/pickup/{orderId}")
    public Result<Void> confirmPickup(@PathVariable Long orderId) {
        if (reservationService.markAsPickedUp(orderId)) {
            return Result.success(null);
        }
        return Result.error("操作失败");
    }

    /**
     * 获取店铺时段配置
     */
    @GetMapping("/config/{shopId}")
    public Result<ShopTimeslotConfig> getConfig(@PathVariable Long shopId) {
        ShopTimeslotConfig config = configMapper.selectByShopId(shopId);
        return Result.success(config);
    }

    /**
     * 更新店铺时段配置
     */
    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody ShopTimeslotConfig config) {
        ShopTimeslotConfig existing = configMapper.selectByShopId(config.getShopId());
        if (existing != null) {
            config.setId(existing.getId());
            configMapper.updateById(config);
        } else {
            configMapper.insert(config);
        }
        return Result.success(null);
    }

    /**
     * 手动生成时段
     */
    @PostMapping("/timeslots/generate/{shopId}")
    public Result<Void> generateTimeslots(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "3") int days) {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            timeslotService.generateTimeslotsForDate(shopId, today.plusDays(i));
        }
        return Result.success(null);
    }

    /**
     * 保存菜品营养信息
     */
    @PostMapping("/dish-nutrition")
    public Result<Void> saveDishNutrition(@RequestBody DishNutrition nutrition) {
        nutritionService.saveOrUpdateByDishId(nutrition);
        return Result.success(null);
    }

    /**
     * 获取菜品营养信息
     */
    @GetMapping("/dish-nutrition/{dishId}")
    public Result<DishNutrition> getDishNutrition(@PathVariable Long dishId) {
        return Result.success(nutritionService.getByDishId(dishId));
    }
}
