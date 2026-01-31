package com.campus.ordering.controller.merchant;

import com.campus.ordering.entity.MarketingCampaign;
import com.campus.ordering.service.MarketingService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/merchant/marketing")
public class MerchantMarketingController {

    @Autowired
    private MarketingService marketingService;

    @Autowired
    private ShopService shopService;

    /**
     * 获取营销活动列表
     */
    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();
        return Result.success(marketingService.findByShopId(shopId));
    }

    /**
     * 创建营销活动
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody MarketingCampaign campaign, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();

        campaign.setShopId(shopId);
        campaign.setStatus(0); // 草稿
        marketingService.save(campaign);
        return Result.success("创建成功", campaign);
    }

    /**
     * 更新营销活动
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody MarketingCampaign campaign) {
        campaign.setId(id);
        marketingService.updateById(campaign);
        return Result.success("更新成功");
    }

    /**
     * 发送营销活动
     */
    @PostMapping("/{id}/send")
    public Result<?> send(@PathVariable Long id) {
        MarketingCampaign campaign = marketingService.getById(id);
        if (campaign == null) {
            return Result.error("活动不存在");
        }
        campaign.setStatus(1); // 待发送
        marketingService.updateById(campaign);
        marketingService.sendCampaign(id);
        return Result.success("发送成功");
    }

    /**
     * 删除营销活动
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        marketingService.removeById(id);
        return Result.success("删除成功");
    }
}
