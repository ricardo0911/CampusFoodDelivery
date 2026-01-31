package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.PlatformActivity;

import java.util.List;

public interface PlatformActivityService extends IService<PlatformActivity> {
    List<PlatformActivity> getActiveActivities();
}
