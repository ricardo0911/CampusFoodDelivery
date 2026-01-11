package com.campus.ordering.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.DishCategory;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.service.DishCategoryService;
import com.campus.ordering.service.DishService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private DishCategoryService dishCategoryService;

    @Autowired
    private DishService dishService;

    /**
     * 获取店铺列表
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sortBy) {

        Page<Shop> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getStatus, 1); // 只查营业中的店铺

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Shop::getName, keyword);
        }

        if ("sales".equals(sortBy)) {
            wrapper.orderByDesc(Shop::getMonthlySales);
        } else if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(Shop::getRating);
        } else {
            wrapper.orderByDesc(Shop::getMonthlySales);
        }

        Page<Shop> result = shopService.page(pageParam, wrapper);
        return Result.success(result);
    }

    /**
     * 获取店铺详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Shop shop = shopService.getById(id);
        if (shop == null) {
            return Result.error("店铺不存在");
        }
        return Result.success(shop);
    }

    /**
     * 获取店铺分类和菜品
     */
    @GetMapping("/{id}/menu")
    public Result<?> menu(@PathVariable Long id) {
        Shop shop = shopService.getById(id);
        if (shop == null) {
            return Result.error("店铺不存在");
        }

        List<DishCategory> categories = dishCategoryService.findByShopId(id);
        List<Dish> dishes = dishService.findByShopId(id);

        Map<String, Object> data = new HashMap<>();
        data.put("shop", shop);
        data.put("categories", categories);
        data.put("dishes", dishes);
        return Result.success(data);
    }

    /**
     * 获取菜品详情
     */
    @GetMapping("/dish/{id}")
    public Result<?> dishDetail(@PathVariable Long id) {
        Dish dish = dishService.getById(id);
        if (dish == null) {
            return Result.error("菜品不存在");
        }
        return Result.success(dish);
    }

    /**
     * 搜索菜品
     */
    @GetMapping("/dish/search")
    public Result<?> searchDish(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Page<Dish> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getStatus, 1)
                .eq(Dish::getAuditStatus, 1)
                .like(Dish::getName, keyword)
                .orderByDesc(Dish::getSales);

        Page<Dish> result = dishService.page(pageParam, wrapper);
        return Result.success(result);
    }
}
