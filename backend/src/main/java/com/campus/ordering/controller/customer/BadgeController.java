package com.campus.ordering.controller.customer;

import com.campus.ordering.entity.Badge;
import com.campus.ordering.service.BadgeService;
import com.campus.ordering.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/customer/badge")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final JwtUtil jwtUtil;

    /**
     * 获取所有徽章（带用户获得状态）
     */
    @GetMapping("/list")
    public ResponseEntity<?> getBadges(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        List<Map<String, Object>> badges = badgeService.getUserBadges(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", badges);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户徽章统计
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getBadgeStats(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Map<String, Object> stats = badgeService.getUserBadgeStats(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", stats);

        return ResponseEntity.ok(result);
    }

    /**
     * 初始化预设徽章
     */
    @PostMapping("/init")
    public ResponseEntity<?> initBadges() {
        badgeService.initDefaultBadges();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "徽章初始化完成");

        return ResponseEntity.ok(result);
    }

    /**
     * 检查并授予徽章（模拟）
     */
    @PostMapping("/check")
    public ResponseEntity<?> checkBadges(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> body) {

        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        int orderCount = body.get("orderCount") != null ? Integer.valueOf(body.get("orderCount").toString()) : 0;
        double totalSpent = body.get("totalSpent") != null ? Double.valueOf(body.get("totalSpent").toString()) : 0;
        String category = (String) body.get("category");
        boolean isFirstOrder = body.get("isFirstOrder") != null &&
                Boolean.valueOf(body.get("isFirstOrder").toString());

        List<Badge> newBadges = badgeService.checkAndGrantBadges(
                userId, orderCount, totalSpent, category, isFirstOrder);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", newBadges);
        result.put("message", newBadges.isEmpty() ? "暂无新徽章" : "恭喜获得新徽章！");

        return ResponseEntity.ok(result);
    }
}
