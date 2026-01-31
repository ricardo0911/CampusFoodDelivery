package com.campus.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.mapper.DishMapper;
import com.campus.ordering.mapper.ShopMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RandomPickService {

    private final ShopMapper shopMapper;
    private final DishMapper dishMapper;

    private final Random random = new Random();

    /**
     * 随机选择一个店铺
     */
    public Shop randomShop(String category) {
        LambdaQueryWrapper<Shop> query = new LambdaQueryWrapper<>();
        query.eq(Shop::getStatus, 1); // 营业中

        // Note: category filtering not available in current Shop entity
        // Would need to add category field to Shop if needed

        List<Shop> shops = shopMapper.selectList(query);
        if (shops.isEmpty()) {
            return null;
        }

        return shops.get(random.nextInt(shops.size()));
    }

    /**
     * 随机选择多个店铺（用于转盘）
     */
    public List<Shop> randomShops(int count) {
        LambdaQueryWrapper<Shop> query = new LambdaQueryWrapper<>();
        query.eq(Shop::getStatus, 1);

        List<Shop> allShops = shopMapper.selectList(query);
        if (allShops.isEmpty()) {
            return Collections.emptyList();
        }

        // 洗牌并取前N个
        Collections.shuffle(allShops);
        int resultCount = Math.min(count, allShops.size());
        return allShops.subList(0, resultCount);
    }

    /**
     * 随机选择一个菜品
     */
    public Dish randomDish(Long shopId) {
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.eq(Dish::getStatus, 1); // 在售

        if (shopId != null) {
            query.eq(Dish::getShopId, shopId);
        }

        List<Dish> dishes = dishMapper.selectList(query);
        if (dishes.isEmpty()) {
            return null;
        }

        return dishes.get(random.nextInt(dishes.size()));
    }

    /**
     * 随机选择多个菜品（用于转盘）
     */
    public List<Dish> randomDishes(Long shopId, int count) {
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.eq(Dish::getStatus, 1);

        if (shopId != null) {
            query.eq(Dish::getShopId, shopId);
        }

        List<Dish> allDishes = dishMapper.selectList(query);
        if (allDishes.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.shuffle(allDishes);
        int resultCount = Math.min(count, allDishes.size());
        return allDishes.subList(0, resultCount);
    }

    /**
     * 获取今日推荐（结合随机+评分）
     */
    public Map<String, Object> getTodayRecommend() {
        Map<String, Object> result = new HashMap<>();

        // 随机高分店铺
        LambdaQueryWrapper<Shop> shopQuery = new LambdaQueryWrapper<>();
        shopQuery.eq(Shop::getStatus, 1)
                .orderByDesc(Shop::getRating)
                .last("LIMIT 10");
        List<Shop> topShops = shopMapper.selectList(shopQuery);

        Shop recommendShop = null;
        if (!topShops.isEmpty()) {
            recommendShop = topShops.get(random.nextInt(topShops.size()));
        }

        result.put("shop", recommendShop);

        // 获取该店铺的推荐菜品
        if (recommendShop != null) {
            List<Dish> dishes = randomDishes(recommendShop.getId(), 3);
            result.put("dishes", dishes);
        }

        // 生成推荐理由
        List<String> reasons = Arrays.asList(
                "今日人气爆棚 🔥",
                "好评如潮 ⭐",
                "超高回头率 💯",
                "小编精选 👍",
                "限时优惠中 🎁");
        result.put("reason", reasons.get(random.nextInt(reasons.size())));

        return result;
    }

    /**
     * 随机挑战（大胃王挑战等）
     */
    public Map<String, Object> getRandomChallenge() {
        List<Map<String, Object>> challenges = new ArrayList<>();

        Map<String, Object> c1 = new HashMap<>();
        c1.put("name", "早餐达人");
        c1.put("description", "连续7天在8点前下单早餐");
        c1.put("reward", "获得【早起鸟】徽章");
        c1.put("icon", "🌅");
        challenges.add(c1);

        Map<String, Object> c2 = new HashMap<>();
        c2.put("name", "辣王挑战");
        c2.put("description", "点3份特辣菜品并完成订单");
        c2.put("reward", "获得【辣王】徽章");
        c2.put("icon", "🌶️");
        challenges.add(c2);

        Map<String, Object> c3 = new HashMap<>();
        c3.put("name", "拼单王");
        c3.put("description", "成功发起3次拼单");
        c3.put("reward", "优惠券5元");
        c3.put("icon", "👥");
        challenges.add(c3);

        Map<String, Object> c4 = new HashMap<>();
        c4.put("name", "探店达人");
        c4.put("description", "在5家不同店铺下单");
        c4.put("reward", "获得【探店达人】徽章");
        c4.put("icon", "🗺️");
        challenges.add(c4);

        return challenges.get(random.nextInt(challenges.size()));
    }
}
