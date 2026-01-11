package com.campus.ordering.controller.merchant;

import com.campus.ordering.entity.Merchant;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.service.MerchantService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/shop")
public class MerchantShopController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private MerchantService merchantService;

    /**
     * 获取店铺信息
     */
    @GetMapping("/info")
    public Result<?> info(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);
        Merchant merchant = merchantService.getById(merchantId);

        Map<String, Object> data = new HashMap<>();
        data.put("shop", shop);
        data.put("merchant", merchant);
        merchant.setPassword(null);

        return Result.success(data);
    }

    /**
     * 更新店铺信息
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Shop shop, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop existing = shopService.findByMerchantId(merchantId);

        if (existing == null) {
            return Result.error("店铺不存在");
        }

        shop.setId(existing.getId());
        shop.setMerchantId(merchantId);
        shopService.updateById(shop);

        return Result.success("更新成功");
    }

    /**
     * 切换营业状态
     */
    @PostMapping("/toggle-status")
    public Result<?> toggleStatus(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Shop shop = shopService.findByMerchantId(merchantId);

        if (shop == null) {
            return Result.error("店铺不存在");
        }

        shop.setStatus(shop.getStatus() == 1 ? 0 : 1);
        shopService.updateById(shop);

        String statusText = shop.getStatus() == 1 ? "营业中" : "休息中";
        return Result.success("已切换为" + statusText, shop);
    }
}
