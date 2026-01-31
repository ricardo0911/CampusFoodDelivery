package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.PlatformActivity;
import com.campus.ordering.mapper.PlatformActivityMapper;
import com.campus.ordering.service.PlatformActivityService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlatformActivityServiceImpl extends ServiceImpl<PlatformActivityMapper, PlatformActivity> implements PlatformActivityService {

    @Override
    public List<PlatformActivity> getActiveActivities() {
        LocalDateTime now = LocalDateTime.now();
        return this.list(new LambdaQueryWrapper<PlatformActivity>()
                .eq(PlatformActivity::getStatus, 1)
                .le(PlatformActivity::getStartTime, now)
                .ge(PlatformActivity::getEndTime, now)
                .orderByDesc(PlatformActivity::getCreatedAt));
    }
}
