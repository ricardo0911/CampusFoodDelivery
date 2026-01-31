package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.MarketingCampaign;

import java.util.List;

public interface MarketingService extends IService<MarketingCampaign> {
    List<MarketingCampaign> findByShopId(Long shopId);
    void sendCampaign(Long campaignId);
}
