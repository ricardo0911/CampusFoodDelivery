package com.campus.ordering.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.DishCategory;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.service.DishCategoryService;
import com.campus.ordering.service.DishService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/dish")
public class MerchantDishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishCategoryService dishCategoryService;

    @Autowired
    private ShopService shopService;

    /**
     * 获取菜品列表
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) Long categoryId, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dish::getShopId, shop.getId());
        if (categoryId != null) {
            wrapper.eq(Dish::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Dish::getCreatedAt);

        List<Dish> dishes = dishService.list(wrapper);
        return Result.success(dishes);
    }

    /**
     * 添加菜品
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Dish dish, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        dish.setShopId(shop.getId());
        dish.setId(null);
        dish.setStatus(1); // 默认上架
        dish.setAuditStatus(1); // 默认审核通过（实际应用中可设为待审核）
        dish.setSales(0);

        dishService.save(dish);
        return Result.success("添加成功", dish);
    }

    /**
     * 更新菜品
     */
    @PutMapping("/update/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Dish dish, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        Dish existing = dishService.getById(id);
        if (existing == null || !existing.getShopId().equals(shop.getId())) {
            return Result.error("菜品不存在");
        }

        dish.setId(id);
        dish.setShopId(shop.getId());
        dishService.updateById(dish);

        return Result.success("更新成功");
    }

    /**
     * 删除菜品
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        Dish existing = dishService.getById(id);
        if (existing == null || !existing.getShopId().equals(shop.getId())) {
            return Result.error("菜品不存在");
        }

        dishService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 切换菜品上下架
     */
    @PostMapping("/toggle-status/{id}")
    public Result<?> toggleStatus(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        Dish dish = dishService.getById(id);
        if (dish == null || !dish.getShopId().equals(shop.getId())) {
            return Result.error("菜品不存在");
        }

        dish.setStatus(dish.getStatus() == 1 ? 0 : 1);
        dishService.updateById(dish);

        String statusText = dish.getStatus() == 1 ? "已上架" : "已下架";
        return Result.success(statusText);
    }

    // ========== 分类管理 ==========

    /**
     * 获取分类列表
     */
    @GetMapping("/category/list")
    public Result<?> categoryList(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        List<DishCategory> categories = dishCategoryService.list(
                new LambdaQueryWrapper<DishCategory>()
                        .eq(DishCategory::getShopId, shop.getId())
                        .orderByAsc(DishCategory::getSortOrder));

        return Result.success(categories);
    }

    /**
     * 添加分类
     */
    @PostMapping("/category/add")
    public Result<?> addCategory(@RequestBody DishCategory category, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        category.setShopId(shop.getId());
        category.setId(null);
        category.setStatus(1);

        dishCategoryService.save(category);
        return Result.success("添加成功", category);
    }

    /**
     * 更新分类
     */
    @PutMapping("/category/update/{id}")
    public Result<?> updateCategory(@PathVariable Long id, @RequestBody DishCategory category,
            HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        DishCategory existing = dishCategoryService.getById(id);
        if (existing == null || !existing.getShopId().equals(shop.getId())) {
            return Result.error("分类不存在");
        }

        category.setId(id);
        category.setShopId(shop.getId());
        dishCategoryService.updateById(category);

        return Result.success("更新成功");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/category/delete/{id}")
    public Result<?> deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        DishCategory existing = dishCategoryService.getById(id);
        if (existing == null || !existing.getShopId().equals(shop.getId())) {
            return Result.error("分类不存在");
        }

        // 检查是否有关联菜品
        long count = dishService.count(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, id));
        if (count > 0) {
            return Result.error("该分类下有关联菜品，无法删除");
        }

        dishCategoryService.removeById(id);
        return Result.success("删除成功");
    }
}
