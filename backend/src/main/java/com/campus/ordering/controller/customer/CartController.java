package com.campus.ordering.controller.customer;

import com.campus.ordering.entity.Cart;
import com.campus.ordering.entity.Dish;
import com.campus.ordering.service.CartService;
import com.campus.ordering.service.DishService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private DishService dishService;

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Cart> carts = cartService.findByUserId(userId);
        return Result.success(carts);
    }

    /**
     * 添加到购物车
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long dishId = Long.valueOf(params.get("dishId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        String specifications = (String) params.get("specifications");

        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            return Result.error("菜品不存在");
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setShopId(dish.getShopId());
        cart.setDishId(dishId);
        cart.setDishName(dish.getName());
        cart.setDishImage(dish.getImage());
        cart.setQuantity(quantity);
        cart.setUnitPrice(dish.getPrice());
        cart.setSpecifications(specifications);

        cartService.save(cart);
        return Result.success("添加成功");
    }

    /**
     * 更新购物车数量
     */
    @PutMapping("/update/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer quantity = Integer.valueOf(params.get("quantity").toString());

        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.error("购物车项不存在");
        }

        if (quantity <= 0) {
            cartService.removeById(id);
            return Result.success("已删除");
        }

        cart.setQuantity(quantity);
        cartService.updateById(cart);
        return Result.success("更新成功");
    }

    /**
     * 删除购物车项
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.error("购物车项不存在");
        }

        cartService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<?> clear(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        cartService.clearByUserId(userId);
        return Result.success("已清空");
    }
}
