package com.campus.ordering.controller.merchant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.Order;
import com.campus.ordering.entity.OrderItem;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.service.OrderItemService;
import com.campus.ordering.service.OrderService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/order")
public class MerchantOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ShopService shopService;

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getShopId, shop.getId());
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreatedAt);

        List<Order> orders = orderService.list(wrapper);
        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        Order order = orderService.getById(id);
        if (order == null || !order.getShopId().equals(shop.getId())) {
            return Result.error("订单不存在");
        }

        List<OrderItem> items = orderItemService.findByOrderId(id);

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return Result.success(data);
    }

    /**
     * 接单
     */
    @PostMapping("/{id}/accept")
    public Result<?> accept(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> params,
            HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        Order order = orderService.getById(id);
        if (order == null || !order.getShopId().equals(shop.getId())) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 1) {
            return Result.error("订单状态不允许接单");
        }

        order.setStatus(2); // 制作中
        order.setAcceptTime(LocalDateTime.now());
        if (params != null && params.containsKey("estimatedTime")) {
            order.setEstimatedTime(Integer.valueOf(params.get("estimatedTime").toString()));
        } else {
            order.setEstimatedTime(15); // 默认15分钟
        }

        orderService.updateById(order);
        return Result.success("接单成功");
    }

    /**
     * 拒绝订单
     */
    @PostMapping("/{id}/reject")
    public Result<?> reject(@PathVariable Long id, @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        Order order = orderService.getById(id);
        if (order == null || !order.getShopId().equals(shop.getId())) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 1) {
            return Result.error("订单状态不允许拒绝");
        }

        order.setStatus(5); // 已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason("商家拒单：" + params.get("reason"));

        orderService.updateById(order);
        return Result.success("已拒绝订单");
    }

    /**
     * 开始配送
     */
    @PostMapping("/{id}/deliver")
    public Result<?> deliver(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        Order order = orderService.getById(id);
        if (order == null || !order.getShopId().equals(shop.getId())) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 2) {
            return Result.error("订单状态不允许配送");
        }

        order.setStatus(3); // 配送中
        orderService.updateById(order);

        return Result.success("已开始配送");
    }

    /**
     * 完成订单
     */
    @PostMapping("/{id}/complete")
    public Result<?> complete(@PathVariable Long id, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        Order order = orderService.getById(id);
        if (order == null || !order.getShopId().equals(shop.getId())) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 3) {
            return Result.error("订单状态不允许完成");
        }

        order.setStatus(4); // 已完成
        order.setFinishTime(LocalDateTime.now());
        orderService.updateById(order);

        // 更新店铺月销量
        shop.setMonthlySales(shop.getMonthlySales() + 1);
        shopService.updateById(shop);

        return Result.success("订单已完成");
    }
}
