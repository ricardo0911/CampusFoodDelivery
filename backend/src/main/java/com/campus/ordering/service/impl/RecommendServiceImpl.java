package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.dto.RecommendResult;
import com.campus.ordering.entity.*;
import com.campus.ordering.mapper.RecommendationLogMapper;
import com.campus.ordering.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private final UserProfileService userProfileService;
    private final DishService dishService;
    private final DishNutritionService nutritionService;
    private final ShopService shopService;
    private final RecommendationLogMapper logMapper;

    @Override
    public RecommendResult getHomeRecommendations(Long userId, int limit) {
        UserProfile profile = userProfileService.getOrCreateByUserId(userId);

        // 获取所有上架菜品
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.eq(Dish::getStatus, 1).eq(Dish::getAuditStatus, 1);
        List<Dish> allDishes = dishService.list(query);

        // 计算推荐分数
        Map<Long, Double> scores = calculateDishScores(userId, allDishes);

        // 按分数排序并取前N个
        List<Dish> recommended = allDishes.stream()
                .sorted((a, b) -> Double.compare(scores.getOrDefault(b.getId(), 0.0),
                        scores.getOrDefault(a.getId(), 0.0)))
                .limit(limit)
                .collect(Collectors.toList());

        // 构建结果
        RecommendResult result = buildResult(recommended, scores, RecommendationLog.TYPE_HOME, "根据你的口味偏好推荐");

        // 记录日志
        saveLog(userId, result, RecommendationLog.TYPE_HOME);

        return result;
    }

    @Override
    public RecommendResult getSearchRecommendations(Long userId, String keyword, int limit) {
        // 关键词搜索
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.eq(Dish::getStatus, 1)
                .eq(Dish::getAuditStatus, 1)
                .and(w -> w.like(Dish::getName, keyword).or().like(Dish::getDescription, keyword));
        List<Dish> dishes = dishService.list(query);

        // 计算分数并排序
        Map<Long, Double> scores = calculateDishScores(userId, dishes);
        List<Dish> recommended = dishes.stream()
                .sorted((a, b) -> Double.compare(scores.getOrDefault(b.getId(), 0.0),
                        scores.getOrDefault(a.getId(), 0.0)))
                .limit(limit)
                .collect(Collectors.toList());

        RecommendResult result = buildResult(recommended, scores, RecommendationLog.TYPE_SEARCH,
                "搜索「" + keyword + "」的结果");
        saveLog(userId, result, RecommendationLog.TYPE_SEARCH);
        return result;
    }

    @Override
    public RecommendResult getCartRecommendations(Long userId, List<Long> cartDishIds, int limit) {
        if (cartDishIds == null || cartDishIds.isEmpty()) {
            return getHomeRecommendations(userId, limit);
        }

        // 获取购物车菜品所属店铺
        List<Dish> cartDishes = dishService.listByIds(cartDishIds);
        Set<Long> shopIds = cartDishes.stream().map(Dish::getShopId).collect(Collectors.toSet());

        // 推荐同店铺的其他菜品
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.eq(Dish::getStatus, 1)
                .eq(Dish::getAuditStatus, 1)
                .in(Dish::getShopId, shopIds)
                .notIn(Dish::getId, cartDishIds);
        List<Dish> dishes = dishService.list(query);

        Map<Long, Double> scores = calculateDishScores(userId, dishes);
        List<Dish> recommended = dishes.stream()
                .sorted((a, b) -> Double.compare(scores.getOrDefault(b.getId(), 0.0),
                        scores.getOrDefault(a.getId(), 0.0)))
                .limit(limit)
                .collect(Collectors.toList());

        RecommendResult result = buildResult(recommended, scores, RecommendationLog.TYPE_CART, "推荐搭配");
        saveLog(userId, result, RecommendationLog.TYPE_CART);
        return result;
    }

    @Override
    public RecommendResult getDetailRecommendations(Long userId, Long dishId, int limit) {
        Dish currentDish = dishService.getById(dishId);
        if (currentDish == null) {
            return new RecommendResult();
        }

        // 获取同分类或同店铺的菜品
        LambdaQueryWrapper<Dish> query = new LambdaQueryWrapper<>();
        query.eq(Dish::getStatus, 1)
                .eq(Dish::getAuditStatus, 1)
                .ne(Dish::getId, dishId)
                .and(w -> w.eq(Dish::getCategoryId, currentDish.getCategoryId())
                        .or().eq(Dish::getShopId, currentDish.getShopId()));
        List<Dish> dishes = dishService.list(query);

        Map<Long, Double> scores = calculateDishScores(userId, dishes);
        List<Dish> recommended = dishes.stream()
                .sorted((a, b) -> Double.compare(scores.getOrDefault(b.getId(), 0.0),
                        scores.getOrDefault(a.getId(), 0.0)))
                .limit(limit)
                .collect(Collectors.toList());

        RecommendResult result = buildResult(recommended, scores, RecommendationLog.TYPE_DETAIL, "相似推荐");
        saveLog(userId, result, RecommendationLog.TYPE_DETAIL);
        return result;
    }

    @Override
    public Map<Long, Double> calculateDishScores(Long userId, List<Dish> dishes) {
        UserProfile profile = userProfileService.getOrCreateByUserId(userId);
        Map<Long, Double> scores = new HashMap<>();

        // 获取营养信息
        List<Long> dishIds = dishes.stream().map(Dish::getId).collect(Collectors.toList());
        Map<Long, DishNutrition> nutritions = nutritionService.getByDishIds(dishIds);

        LocalTime now = LocalTime.now();
        boolean isMealTime = (now.isAfter(LocalTime.of(11, 0)) && now.isBefore(LocalTime.of(13, 30))) ||
                (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(19, 30)));

        for (Dish dish : dishes) {
            double score = 0.0;

            // 1. 口味匹配 (权重0.3)
            score += calculateTasteMatch(dish, profile, nutritions.get(dish.getId())) * 0.3;

            // 2. 常去店铺加分 (权重0.2)
            if (profile.getPreferredShops() != null && profile.getPreferredShops().contains(dish.getShopId())) {
                score += 0.2;
            }

            // 3. 时段热度 (权重0.2)
            if (isMealTime) {
                score += (dish.getSales() != null ? Math.min(dish.getSales() / 1000.0, 1.0) : 0) * 0.2;
            }

            // 4. 价格匹配 (权重0.15)
            if (profile.getAvgOrderAmount() != null && dish.getPrice() != null) {
                double priceDiff = Math.abs(dish.getPrice().doubleValue() - profile.getAvgOrderAmount().doubleValue());
                score += Math.max(0, 1 - priceDiff / 30.0) * 0.15;
            }

            // 5. 健康指数 (权重0.15)
            score += calculateHealthScore(dish, profile, nutritions.get(dish.getId())) * 0.15;

            // 6. 过敏原检查（如果包含过敏原则大幅降分）
            if (containsAllergen(dish, profile, nutritions.get(dish.getId()))) {
                score *= 0.1; // 降低到10%
            }

            scores.put(dish.getId(), score);
        }

        return scores;
    }

    private double calculateTasteMatch(Dish dish, UserProfile profile, DishNutrition nutrition) {
        if (profile.getCuisinePreferences() == null || profile.getCuisinePreferences().isEmpty()) {
            return 0.5; // 无偏好时返回中等分数
        }

        double match = 0.0;
        String desc = dish.getDescription() != null ? dish.getDescription() : "";

        for (Map.Entry<String, Double> pref : profile.getCuisinePreferences().entrySet()) {
            if (desc.contains(pref.getKey()) || dish.getName().contains(pref.getKey())) {
                match += pref.getValue();
            }
        }

        return Math.min(match, 1.0);
    }

    private double calculateHealthScore(Dish dish, UserProfile profile, DishNutrition nutrition) {
        if (nutrition == null || profile.getHealthGoal() == null) {
            return 0.5;
        }

        double score = 0.5;
        List<String> tags = nutrition.getTags();
        if (tags == null)
            return score;

        switch (profile.getHealthGoal()) {
            case 1: // 减脂
                if (tags.contains("低脂") || tags.contains("低热量"))
                    score += 0.3;
                if (tags.contains("高蛋白"))
                    score += 0.2;
                break;
            case 2: // 增肌
                if (tags.contains("高蛋白"))
                    score += 0.4;
                if (tags.contains("高热量"))
                    score += 0.1;
                break;
            case 3: // 均衡
                if (tags.contains("均衡"))
                    score += 0.3;
                break;
        }

        return Math.min(score, 1.0);
    }

    private boolean containsAllergen(Dish dish, UserProfile profile, DishNutrition nutrition) {
        if (profile.getAllergyInfo() == null || profile.getAllergyInfo().isEmpty()) {
            return false;
        }
        if (nutrition == null || nutrition.getAllergens() == null) {
            return false;
        }

        for (String allergen : profile.getAllergyInfo()) {
            if (nutrition.getAllergens().contains(allergen)) {
                return true;
            }
        }
        return false;
    }

    private RecommendResult buildResult(List<Dish> dishes, Map<Long, Double> scores, String type, String reason) {
        RecommendResult result = new RecommendResult();
        result.setType(type);
        result.setReason(reason);

        List<Long> dishIds = dishes.stream().map(Dish::getId).collect(Collectors.toList());
        Map<Long, DishNutrition> nutritions = nutritionService.getByDishIds(dishIds);

        List<RecommendResult.RecommendedDish> recommended = new ArrayList<>();
        for (Dish dish : dishes) {
            RecommendResult.RecommendedDish rd = new RecommendResult.RecommendedDish();
            rd.setId(dish.getId());
            rd.setName(dish.getName());
            rd.setImage(dish.getImage());
            rd.setDescription(dish.getDescription());
            rd.setPrice(dish.getPrice().doubleValue());
            rd.setSales(dish.getSales());
            rd.setScore(scores.getOrDefault(dish.getId(), 0.0));
            rd.setShopId(dish.getShopId());

            // 设置店铺名称
            Shop shop = shopService.getById(dish.getShopId());
            if (shop != null) {
                rd.setShopName(shop.getName());
            }

            // 设置推荐标签
            List<String> tags = new ArrayList<>();
            if (dish.getSales() != null && dish.getSales() > 500)
                tags.add("热销");
            if (dish.getIsSpecial() == 1)
                tags.add("特价");
            rd.setRecommendTags(tags);

            // 设置营养摘要
            DishNutrition nutrition = nutritions.get(dish.getId());
            if (nutrition != null) {
                RecommendResult.NutritionSummary summary = new RecommendResult.NutritionSummary();
                summary.setCalories(nutrition.getCalories());
                if (nutrition.getTags() != null) {
                    summary.setTags(String.join(" ", nutrition.getTags()));
                }
                rd.setNutrition(summary);
            }

            recommended.add(rd);
        }

        result.setDishes(recommended);
        return result;
    }

    private void saveLog(Long userId, RecommendResult result, String type) {
        RecommendationLog log = new RecommendationLog();
        log.setUserId(userId);
        log.setRecommendType(type);
        log.setRecommendReason(result.getReason());
        log.setDishIds(
                result.getDishes().stream().map(RecommendResult.RecommendedDish::getId).collect(Collectors.toList()));

        Map<Long, Double> scoreMap = new HashMap<>();
        for (RecommendResult.RecommendedDish dish : result.getDishes()) {
            scoreMap.put(dish.getId(), dish.getScore());
        }
        log.setDishScores(scoreMap);

        Map<String, Object> context = new HashMap<>();
        context.put("time", LocalDateTime.now().toString());
        context.put("hour", LocalDateTime.now().getHour());
        log.setSceneContext(context);

        logMapper.insert(log);
        result.setLogId(log.getId());
    }

    @Override
    public void recordClick(Long logId, Long dishId) {
        RecommendationLog log = logMapper.selectById(logId);
        if (log != null) {
            log.setClickedDishId(dishId);
            log.setClickTime(LocalDateTime.now());
            logMapper.updateById(log);
        }
    }

    @Override
    public void recordOrder(Long logId, Long dishId) {
        RecommendationLog log = logMapper.selectById(logId);
        if (log != null) {
            log.setOrderedDishId(dishId);
            log.setOrderTime(LocalDateTime.now());
            logMapper.updateById(log);
        }
    }
}
