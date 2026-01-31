package com.campus.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.Badge;
import com.campus.ordering.entity.UserBadge;
import com.campus.ordering.mapper.BadgeMapper;
import com.campus.ordering.mapper.UserBadgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeMapper badgeMapper;
    private final UserBadgeMapper userBadgeMapper;

    /**
     * 获取所有徽章定义
     */
    public List<Badge> getAllBadges() {
        LambdaQueryWrapper<Badge> query = new LambdaQueryWrapper<>();
        query.eq(Badge::getStatus, 1)
                .orderByAsc(Badge::getSortOrder);
        return badgeMapper.selectList(query);
    }

    /**
     * 获取用户已获得的徽章
     */
    public List<Map<String, Object>> getUserBadges(Long userId) {
        // 获取所有徽章
        List<Badge> allBadges = getAllBadges();

        // 获取用户已获得的徽章
        LambdaQueryWrapper<UserBadge> query = new LambdaQueryWrapper<>();
        query.eq(UserBadge::getUserId, userId);
        List<UserBadge> userBadges = userBadgeMapper.selectList(query);

        Set<Long> obtainedIds = new HashSet<>();
        Map<Long, LocalDateTime> obtainedTimes = new HashMap<>();
        for (UserBadge ub : userBadges) {
            obtainedIds.add(ub.getBadgeId());
            obtainedTimes.put(ub.getBadgeId(), ub.getObtainedAt());
        }

        // 组装结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Badge badge : allBadges) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", badge.getId());
            item.put("name", badge.getName());
            item.put("description", badge.getDescription());
            item.put("icon", badge.getIcon());
            item.put("type", badge.getType());
            item.put("rarity", badge.getRarity());
            item.put("obtained", obtainedIds.contains(badge.getId()));
            item.put("obtainedAt", obtainedTimes.get(badge.getId()));
            result.add(item);
        }

        return result;
    }

    /**
     * 获取用户徽章统计
     */
    public Map<String, Object> getUserBadgeStats(Long userId) {
        List<Badge> allBadges = getAllBadges();

        LambdaQueryWrapper<UserBadge> query = new LambdaQueryWrapper<>();
        query.eq(UserBadge::getUserId, userId);
        Long obtainedCount = userBadgeMapper.selectCount(query);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allBadges.size());
        stats.put("obtained", obtainedCount);
        stats.put("progress", allBadges.isEmpty() ? 0 : (obtainedCount * 100 / allBadges.size()));

        return stats;
    }

    /**
     * 检查并授予徽章（订单完成后调用）
     */
    @Transactional
    public List<Badge> checkAndGrantBadges(Long userId, int orderCount, double totalSpent,
            String category, boolean isFirstOrder) {
        List<Badge> newBadges = new ArrayList<>();
        List<Badge> allBadges = getAllBadges();

        // 获取用户已有徽章
        LambdaQueryWrapper<UserBadge> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(UserBadge::getUserId, userId);
        List<UserBadge> existingBadges = userBadgeMapper.selectList(existQuery);
        Set<Long> existingIds = new HashSet<>();
        for (UserBadge ub : existingBadges) {
            existingIds.add(ub.getBadgeId());
        }

        for (Badge badge : allBadges) {
            // 已获得的跳过
            if (existingIds.contains(badge.getId())) {
                continue;
            }

            boolean shouldGrant = checkCondition(badge, orderCount, totalSpent, category, isFirstOrder);

            if (shouldGrant) {
                UserBadge userBadge = new UserBadge();
                userBadge.setUserId(userId);
                userBadge.setBadgeId(badge.getId());
                userBadge.setObtainedAt(LocalDateTime.now());
                userBadge.setDisplayed(0);
                userBadge.setCreatedAt(LocalDateTime.now());
                userBadgeMapper.insert(userBadge);

                newBadges.add(badge);
            }
        }

        return newBadges;
    }

    /**
     * 授予特定徽章
     */
    @Transactional
    public boolean grantBadge(Long userId, Long badgeId) {
        // 检查是否已有
        LambdaQueryWrapper<UserBadge> query = new LambdaQueryWrapper<>();
        query.eq(UserBadge::getUserId, userId)
                .eq(UserBadge::getBadgeId, badgeId);
        if (userBadgeMapper.selectCount(query) > 0) {
            return false;
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUserId(userId);
        userBadge.setBadgeId(badgeId);
        userBadge.setObtainedAt(LocalDateTime.now());
        userBadge.setDisplayed(0);
        userBadge.setCreatedAt(LocalDateTime.now());
        userBadgeMapper.insert(userBadge);

        return true;
    }

    // 检查徽章获取条件
    private boolean checkCondition(Badge badge, int orderCount, double totalSpent,
            String category, boolean isFirstOrder) {
        String conditionType = badge.getConditionType();
        Integer conditionValue = badge.getConditionValue();

        if (conditionType == null)
            return false;

        switch (conditionType) {
            case "first_order":
                return isFirstOrder;
            case "order_count":
                return orderCount >= conditionValue;
            case "total_spent":
                return totalSpent >= conditionValue;
            case "category_drinks":
                return "饮品".equals(category);
            case "category_dessert":
                return "甜点".equals(category);
            case "category_fastfood":
                return "快餐".equals(category);
            default:
                return false;
        }
    }

    /**
     * 初始化预设徽章（如果不存在）
     */
    @Transactional
    public void initDefaultBadges() {
        LambdaQueryWrapper<Badge> query = new LambdaQueryWrapper<>();
        if (badgeMapper.selectCount(query) > 0) {
            return; // 已有数据，不初始化
        }

        List<Badge> badges = Arrays.asList(
                createBadge("新手上路", "完成第一单", "🎉", 1, "first_order", 1, 1, 1),
                createBadge("常客", "累计下单5次", "⭐", 1, "order_count", 5, 1, 2),
                createBadge("老顾客", "累计下单20次", "🌟", 1, "order_count", 20, 2, 3),
                createBadge("美食达人", "累计下单50次", "👑", 1, "order_count", 50, 3, 4),
                createBadge("传说食客", "累计下单100次", "🏆", 1, "order_count", 100, 4, 5),
                createBadge("小确幸", "累计消费满100元", "💰", 2, "total_spent", 100, 1, 10),
                createBadge("大手笔", "累计消费满500元", "💎", 2, "total_spent", 500, 2, 11),
                createBadge("土豪金", "累计消费满1000元", "🏅", 2, "total_spent", 1000, 3, 12),
                createBadge("饮品爱好者", "购买过饮品类商品", "🧋", 4, "category_drinks", 1, 1, 20),
                createBadge("甜品控", "购买过甜点类商品", "🍰", 4, "category_dessert", 1, 1, 21),
                createBadge("快餐达人", "购买过快餐类商品", "🍔", 4, "category_fastfood", 1, 1, 22));

        for (Badge badge : badges) {
            badgeMapper.insert(badge);
        }
    }

    private Badge createBadge(String name, String desc, String icon, int type,
            String condType, int condValue, int rarity, int sort) {
        Badge badge = new Badge();
        badge.setName(name);
        badge.setDescription(desc);
        badge.setIcon(icon);
        badge.setType(type);
        badge.setConditionType(condType);
        badge.setConditionValue(condValue);
        badge.setRarity(rarity);
        badge.setSortOrder(sort);
        badge.setStatus(1);
        badge.setCreatedAt(LocalDateTime.now());
        badge.setUpdatedAt(LocalDateTime.now());
        return badge;
    }
}
