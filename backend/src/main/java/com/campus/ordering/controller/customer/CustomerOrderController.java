package com.campus.ordering.controller.customer;

import com.campus.ordering.dto.CreateOrderDTO;
import com.campus.ordering.entity.*;
import com.campus.ordering.service.*;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/order")
public class CustomerOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private DishService dishService;

    /**
     * 获取订单列表
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Order> orders = orderService.findByUserId(userId);

        if (status != null) {
            orders.removeIf(o -> !o.getStatus().equals(status));
        }

        return Result.success(orders);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.getById(id);

        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }

        List<OrderItem> items = orderItemService.findByOrderId(id);

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return Result.success(data);
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    @Transactional
    public Result<?> create(@RequestBody CreateOrderDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Shop shop = shopService.getById(dto.getShopId());
        if (shop == null || shop.getStatus() != 1) {
            return Result.error("店铺不存在或已休息");
        }

        Address address = addressService.getById(dto.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.error("地址不存在");
        }

        // 计算金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderDTO.OrderItemDTO item : dto.getItems()) {
            BigDecimal subtotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        BigDecimal deliveryFee = shop.getDeliveryFee();
        BigDecimal payAmount = totalAmount.add(deliveryFee);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderService.generateOrderNo());
        order.setUserId(userId);
        order.setShopId(shop.getId());
        order.setShopName(shop.getName());
        order.setTotalAmount(totalAmount);
        order.setDeliveryFee(deliveryFee);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(payAmount);
        order.setAddressId(address.getId());
        order.setAddressDetail(address.getDetail());
        order.setContactName(address.getContactName());
        order.setContactPhone(address.getPhone());
        order.setRemark(dto.getRemark());
        order.setStatus(0); // 待支付

        orderService.save(order);

        // 创建订单明细
        for (CreateOrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Dish dish = dishService.getById(itemDTO.getDishId());

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setDishId(itemDTO.getDishId());
            item.setDishName(dish != null ? dish.getName() : "未知菜品");
            item.setDishImage(dish != null ? dish.getImage() : null);
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setSubtotal(itemDTO.getUnitPrice().multiply(new BigDecimal(itemDTO.getQuantity())));
            item.setSpecifications(itemDTO.getSpecifications());

            orderItemService.save(item);
        }

        // 清空购物车
        cartService.clearByUserId(userId);

        return Result.success("下单成功", order);
    }

    /**
     * 支付订单（模拟）
     */
    @PostMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.getById(id);

        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.error("订单状态不允许支付");
        }

        order.setStatus(1); // 待接单
        order.setPayTime(LocalDateTime.now());
        orderService.updateById(order);

        return Result.success("支付成功");
    }

    /**
     * 取消订单
     */
    @PostMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id, @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.getById(id);

        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() > 1) {
            return Result.error("订单状态不允许取消");
        }

        order.setStatus(5); // 已取消
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(params.get("reason"));
        orderService.updateById(order);

        return Result.success("取消成功");
    }

    /**
     * 确认收货
     */
    @PostMapping("/{id}/confirm")
    public Result<?> confirm(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Order order = orderService.getById(id);

        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 3) {
            return Result.error("订单状态不允许确认收货");
        }

        order.setStatus(4); // 已完成
        order.setFinishTime(LocalDateTime.now());
        orderService.updateById(order);

        return Result.success("确认收货成功");
    }
}
