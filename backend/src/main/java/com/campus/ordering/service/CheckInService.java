package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.CheckIn;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CheckInService extends IService<CheckIn> {
    CheckIn findByUserIdAndDate(Long userId, LocalDate date);
    List<CheckIn> findByUserIdAndMonth(Long userId, int year, int month);
    Map<String, Object> sign(Long userId);
    Map<String, Object> getStatus(Long userId);
    int getContinuousDays(Long userId);
}
