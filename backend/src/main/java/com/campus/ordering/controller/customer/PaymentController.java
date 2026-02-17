package com.campus.ordering.controller.customer;

import com.campus.ordering.entity.User;
import com.campus.ordering.service.UserService;
import com.campus.ordering.util.JwtUtil;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/user")
public class PaymentController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @PostMapping("/recharge")
    public Result<?> recharge(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = getUserId(request);

        BigDecimal amount = new BigDecimal(params.get("amount").toString());
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(new BigDecimal("1000")) > 0) {
            return Result.error(400, "充值金额必须在0-1000元之间");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        BigDecimal newBalance = (user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO).add(amount);
        user.setBalance(newBalance);
        userService.updateById(user);

        Map<String, Object> data = new HashMap<>();
        data.put("balance", newBalance);
        data.put("rechargeAmount", amount);

        return Result.success("充值成功", data);
    }

    @GetMapping("/balance")
    public Result<?> getBalance(HttpServletRequest request) {
        Long userId = getUserId(request);

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("balance", user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO);

        return Result.success(data);
    }
}
