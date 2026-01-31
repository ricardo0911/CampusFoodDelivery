package com.campus.ordering.controller.customer;

import com.campus.ordering.entity.GroupOrder;
import com.campus.ordering.service.GroupOrderService;
import com.campus.ordering.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/customer/group-order")
@RequiredArgsConstructor
public class GroupOrderController {

    private final GroupOrderService groupOrderService;
    private final JwtUtil jwtUtil;

    /**
     * 创建拼单
     */
    @PostMapping("/create")
    public ResponseEntity<?> createGroupOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {

        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        String userName = (String) body.getOrDefault("userName", "用户" + userId);
        Long shopId = Long.valueOf(body.get("shopId").toString());
        String shopName = (String) body.get("shopName");
        String title = (String) body.get("title");
        Integer maxMembers = body.get("maxMembers") != null ? Integer.valueOf(body.get("maxMembers").toString()) : 4;
        BigDecimal targetAmount = body.get("targetAmount") != null ? new BigDecimal(body.get("targetAmount").toString())
                : new BigDecimal("50");
        Integer discountType = body.get("discountType") != null ? Integer.valueOf(body.get("discountType").toString())
                : 3;
        BigDecimal discountValue = body.get("discountValue") != null
                ? new BigDecimal(body.get("discountValue").toString())
                : new BigDecimal("5");

        GroupOrder group = groupOrderService.createGroupOrder(
                userId, userName, shopId, shopName, title,
                maxMembers, targetAmount, discountType, discountValue);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "创建成功");
        result.put("data", group);

        return ResponseEntity.ok(result);
    }

    /**
     * 加入拼单
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinGroupOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {

        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        String groupCode = (String) body.get("groupCode");
        String nickname = (String) body.getOrDefault("nickname", "用户" + userId);
        String avatar = (String) body.get("avatar");

        Map<String, Object> result = groupOrderService.joinGroupOrder(groupCode, userId, nickname, avatar);

        Map<String, Object> response = new HashMap<>();
        if ((Boolean) result.get("success")) {
            response.put("code", 200);
            response.put("message", result.get("message"));
            response.put("data", result.get("groupOrder"));
        } else {
            response.put("code", 400);
            response.put("message", result.get("message"));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取拼单详情
     */
    @GetMapping("/detail/{groupCode}")
    public ResponseEntity<?> getGroupOrderDetail(@PathVariable String groupCode) {
        Map<String, Object> detail = groupOrderService.getGroupOrderDetail(groupCode);

        Map<String, Object> result = new HashMap<>();
        if (detail != null) {
            result.put("code", 200);
            result.put("data", detail);
        } else {
            result.put("code", 404);
            result.put("message", "拼单不存在");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 更新购物车
     */
    @PostMapping("/update-cart")
    public ResponseEntity<?> updateCart(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {

        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Long groupOrderId = Long.valueOf(body.get("groupOrderId").toString());
        String cartItems = (String) body.get("cartItems");
        BigDecimal subtotal = new BigDecimal(body.get("subtotal").toString());

        groupOrderService.updateMemberCart(groupOrderId, userId, cartItems, subtotal);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "更新成功");

        return ResponseEntity.ok(result);
    }

    /**
     * 获取我的拼单列表
     */
    @GetMapping("/my-list")
    public ResponseEntity<?> getMyGroupOrders(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        List<GroupOrder> list = groupOrderService.getUserGroupOrders(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", list);

        return ResponseEntity.ok(result);
    }
}
