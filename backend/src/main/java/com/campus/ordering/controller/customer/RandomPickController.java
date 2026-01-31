package com.campus.ordering.controller.customer;

import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.service.RandomPickService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/customer/random")
@RequiredArgsConstructor
public class RandomPickController {

    private final RandomPickService randomPickService;

    /**
     * 随机选择一个店铺
     */
    @GetMapping("/shop")
    public ResponseEntity<?> randomShop(@RequestParam(required = false) String category) {
        Shop shop = randomPickService.randomShop(category);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", shop);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取转盘店铺列表
     */
    @GetMapping("/wheel-shops")
    public ResponseEntity<?> getWheelShops(@RequestParam(defaultValue = "8") int count) {
        List<Shop> shops = randomPickService.randomShops(count);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", shops);

        return ResponseEntity.ok(result);
    }

    /**
     * 随机选择一个菜品
     */
    @GetMapping("/dish")
    public ResponseEntity<?> randomDish(@RequestParam(required = false) Long shopId) {
        Dish dish = randomPickService.randomDish(shopId);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", dish);

        return ResponseEntity.ok(result);
    }

    /**
     * 获取转盘菜品列表
     */
    @GetMapping("/wheel-dishes")
    public ResponseEntity<?> getWheelDishes(
            @RequestParam(required = false) Long shopId,
            @RequestParam(defaultValue = "8") int count) {
        List<Dish> dishes = randomPickService.randomDishes(shopId, count);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", dishes);

        return ResponseEntity.ok(result);
    }

    /**
     * 今日推荐
     */
    @GetMapping("/today-recommend")
    public ResponseEntity<?> getTodayRecommend() {
        Map<String, Object> recommend = randomPickService.getTodayRecommend();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", recommend);

        return ResponseEntity.ok(result);
    }

    /**
     * 随机挑战
     */
    @GetMapping("/challenge")
    public ResponseEntity<?> getRandomChallenge() {
        Map<String, Object> challenge = randomPickService.getRandomChallenge();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", challenge);

        return ResponseEntity.ok(result);
    }

    /**
     * 模拟店铺数据（用于纯前端演示）
     */
    @GetMapping("/mock-shops")
    public ResponseEntity<?> getMockShops(@RequestParam(defaultValue = "8") int count) {
        List<Map<String, Object>> shops = new ArrayList<>();

        String[] names = { "黄焖鸡米饭", "兰州拉面馆", "麻辣香锅", "沙县小吃",
                "杨国福麻辣烫", "蜜雪冰城", "瑞幸咖啡", "肯德基",
                "麦当劳", "必胜客", "海底捞", "呷哺呷哺" };
        String[] icons = { "🍚", "🍜", "🍲", "🥟", "🍢", "🧋", "☕", "🍔", "🍟", "🍕", "🥘", "🍖" };

        Random random = new Random();
        for (int i = 0; i < Math.min(count, names.length); i++) {
            Map<String, Object> shop = new HashMap<>();
            shop.put("id", i + 1);
            shop.put("name", names[i]);
            shop.put("icon", icons[i]);
            shop.put("rating", 4.0 + random.nextDouble() * 0.9);
            shop.put("deliveryTime", 15 + random.nextInt(30));
            shops.add(shop);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", shops);

        return ResponseEntity.ok(result);
    }
}
