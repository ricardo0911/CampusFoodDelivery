package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.CheckIn;
import com.campus.ordering.entity.User;
import com.campus.ordering.mapper.CheckInMapper;
import com.campus.ordering.mapper.UserMapper;
import com.campus.ordering.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements CheckInService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public CheckIn findByUserIdAndDate(Long userId, LocalDate date) {
        return this.getOne(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .eq(CheckIn::getCheckInDate, date));
    }

    @Override
    public List<CheckIn> findByUserIdAndMonth(Long userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return this.list(new LambdaQueryWrapper<CheckIn>()
                .eq(CheckIn::getUserId, userId)
                .between(CheckIn::getCheckInDate, startDate, endDate)
                .orderByAsc(CheckIn::getCheckInDate));
    }

    @Override
    @Transactional
    public Map<String, Object> sign(Long userId) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        // 检查今天是否已签到
        CheckIn existing = findByUserIdAndDate(userId, today);
        if (existing != null) {
            result.put("success", false);
            result.put("message", "今天已经签到过了");
            return result;
        }

        // 计算连续签到天数
        int continuousDays = getContinuousDays(userId) + 1;

        // 计算积分奖励（连续签到奖励）
        int points = calculatePoints(continuousDays);

        // 创建签到记录
        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckInDate(today);
        checkIn.setPointsEarned(points);
        checkIn.setContinuousDays(continuousDays);
        this.save(checkIn);

        // 更新用户积分（如果User表有points字段）
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 假设User表有points字段，这里需要根据实际情况调整
            // user.setPoints(user.getPoints() + points);
            // userMapper.updateById(user);
        }

        result.put("success", true);
        result.put("message", "签到成功");
        result.put("points", points);
        result.put("continuousDays", continuousDays);
        result.put("checkIn", checkIn);
        return result;
    }

    @Override
    public Map<String, Object> getStatus(Long userId) {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        CheckIn todayCheckIn = findByUserIdAndDate(userId, today);
        result.put("signedToday", todayCheckIn != null);
        result.put("continuousDays", getContinuousDays(userId));

        // 获取本月签到记录
        List<CheckIn> monthRecords = findByUserIdAndMonth(userId, today.getYear(), today.getMonthValue());
        result.put("monthRecords", monthRecords);
        result.put("monthSignCount", monthRecords.size());

        return result;
    }

    @Override
    public int getContinuousDays(Long userId) {
        LocalDate today = LocalDate.now();
        int days = 0;

        // 从昨天开始往前查
        LocalDate checkDate = today.minusDays(1);
        while (true) {
            CheckIn checkIn = findByUserIdAndDate(userId, checkDate);
            if (checkIn == null) {
                break;
            }
            days++;
            checkDate = checkDate.minusDays(1);
        }

        // 如果今天已签到，加1
        CheckIn todayCheckIn = findByUserIdAndDate(userId, today);
        if (todayCheckIn != null) {
            days++;
        }

        return days;
    }

    private int calculatePoints(int continuousDays) {
        // 基础积分10分，连续签到额外奖励
        int basePoints = 10;
        if (continuousDays >= 7) {
            return basePoints + 20; // 连续7天额外20分
        } else if (continuousDays >= 3) {
            return basePoints + 10; // 连续3天额外10分
        }
        return basePoints;
    }
}
