package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.UserProfile;

public interface UserProfileService extends IService<UserProfile> {

    /**
     * 获取用户画像，不存在则创建默认画像
     */
    UserProfile getOrCreateByUserId(Long userId);

    /**
     * 根据订单历史更新用户画像
     */
    void updateProfileFromOrder(Long userId, Long orderId);

    /**
     * 更新用户健康目标
     */
    void updateHealthGoal(Long userId, Integer healthGoal, Integer dailyCalorieTarget);

    /**
     * 添加过敏信息
     */
    void addAllergyInfo(Long userId, String allergen);

    /**
     * 移除过敏信息
     */
    void removeAllergyInfo(Long userId, String allergen);
}
