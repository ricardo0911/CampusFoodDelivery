package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.MarketingCampaign;
import com.campus.ordering.mapper.MarketingCampaignMapper;
import com.campus.ordering.service.MarketingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MarketingServiceImpl extends ServiceImpl<MarketingCampaignMapper, MarketingCampaign> implements MarketingService {

    @Override
    public List<MarketingCampaign> findByShopId(Long shopId) {
        return this.list(new LambdaQueryWrapper<MarketingCampaign>()
                .eq(MarketingCampaign::getShopId, shopId)
                .orderByDesc(MarketingCampaign::getCreatedAt));
    }

    @Override
    public void sendCampaign(Long campaignId) {
        MarketingCampaign campaign = this.getById(campaignId);
        if (campaign == null || campaign.getStatus() != 1) {
            return;
        }

        // 这里实现发送逻辑（如推送通知、发送短信等）
        // 简化实现：直接更新状态为已发送
        campaign.setStatus(2);
        campaign.setSendTime(LocalDateTime.now());
        this.updateById(campaign);
    }
}
