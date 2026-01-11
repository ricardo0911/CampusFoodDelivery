package com.campus.ordering.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ordering.entity.*;
import com.campus.ordering.service.*;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private ShopService shopService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private DishService dishService;

    // 仪表盘统计
    @GetMapping("/dashboard")
    public Result<?> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("userCount", userService.count());
        data.put("merchantCount", merchantService.count());
        data.put("orderCount", orderService.count());
        data.put("todayOrders", orderService.count(new LambdaQueryWrapper<Order>()
                .ge(Order::getCreatedAt, LocalDate.now().atStartOfDay())));
        return Result.success(data);
    }

    // 用户管理
    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return Result.success(userService.page(pageParam, wrapper));
    }

    @PostMapping("/user/{id}/toggle-status")
    public Result<?> toggleUserStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null)
            return Result.error("用户不存在");
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        userService.updateById(user);
        return Result.success(user.getStatus() == 1 ? "已启用" : "已禁用");
    }

    // 商家管理
    @GetMapping("/merchant/list")
    public Result<?> merchantList(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<Merchant> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        if (status != null)
            wrapper.eq(Merchant::getStatus, status);
        wrapper.orderByDesc(Merchant::getCreatedAt);
        return Result.success(merchantService.page(pageParam, wrapper));
    }

    @PostMapping("/merchant/{id}/audit")
    public Result<?> auditMerchant(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Merchant merchant = merchantService.getById(id);
        if (merchant == null)
            return Result.error("商家不存在");
        Boolean approved = (Boolean) params.get("approved");
        merchant.setStatus(approved ? 1 : 2);
        if (!approved)
            merchant.setRejectReason((String) params.get("reason"));
        merchantService.updateById(merchant);
        if (approved) {
            Shop shop = shopService.findByMerchantId(id);
            if (shop != null) {
                shop.setStatus(1);
                shopService.updateById(shop);
            }
        }
        return Result.success(approved ? "审核通过" : "已驳回");
    }

    // 订单管理
    @GetMapping("/order/list")
    public Result<?> orderList(@RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null)
            wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreatedAt);
        return Result.success(orderService.page(pageParam, wrapper));
    }
}
