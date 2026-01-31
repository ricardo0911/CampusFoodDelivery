package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Announcement;

import java.util.List;

public interface AnnouncementService extends IService<Announcement> {
    List<Announcement> getActiveAnnouncements(Integer target);
    List<Announcement> getPopupAnnouncements(Integer target);
}
