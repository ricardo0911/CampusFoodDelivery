package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Announcement;
import com.campus.ordering.mapper.AnnouncementMapper;
import com.campus.ordering.service.AnnouncementService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public List<Announcement> getActiveAnnouncements(Integer target) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .le(Announcement::getStartTime, now)
                .ge(Announcement::getEndTime, now)
                .orderByDesc(Announcement::getCreatedAt);

        if (target != null) {
            wrapper.and(w -> w.eq(Announcement::getTarget, 0).or().eq(Announcement::getTarget, target));
        }

        return this.list(wrapper);
    }

    @Override
    public List<Announcement> getPopupAnnouncements(Integer target) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .eq(Announcement::getIsPopup, 1)
                .le(Announcement::getStartTime, now)
                .ge(Announcement::getEndTime, now)
                .orderByDesc(Announcement::getCreatedAt);

        if (target != null) {
            wrapper.and(w -> w.eq(Announcement::getTarget, 0).or().eq(Announcement::getTarget, target));
        }

        return this.list(wrapper);
    }
}
