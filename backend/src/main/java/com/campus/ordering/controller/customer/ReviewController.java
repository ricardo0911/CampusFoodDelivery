package com.campus.ordering.controller.customer;

import com.campus.ordering.entity.Review;
import com.campus.ordering.entity.Order;
import com.campus.ordering.service.ReviewService;
import com.campus.ordering.service.OrderService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/customer/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderService orderService;

    /**
     * 获取我的评价列表
     */
    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Review> reviews = reviewService.findByUserId(userId);
        return Result.success(reviews);
    }

    /**
     * 获取店铺评价
     */
    @GetMapping("/shop/{shopId}")
    public Result<?> shopReviews(@PathVariable Long shopId) {
        List<Review> reviews = reviewService.findByShopId(shopId);
        return Result.success(reviews);
    }

    /**
     * 提交评价
     */
    @PostMapping("/submit")
    public Result<?> submit(@RequestBody Review review, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Order order = orderService.getById(review.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.error("订单不存在");
        }
        if (order.getStatus() != 4) {
            return Result.error("订单未完成，无法评价");
        }

        review.setUserId(userId);
        review.setShopId(order.getShopId());
        review.setStatus(1);

        reviewService.save(review);
        return Result.success("评价成功");
    }
}
