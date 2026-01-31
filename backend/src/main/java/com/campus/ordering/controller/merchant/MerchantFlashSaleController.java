package com.campus.ordering.controller.merchant;

import com.campus.ordering.entity.FlashSale;
import com.campus.ordering.service.FlashSaleService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/merchant/flash-sale")
public class MerchantFlashSaleController {

    @Autowired
    private FlashSaleService flashSaleService;

    @Autowired
    private ShopService shopService;

    /**
     * 获取店铺秒杀活动列表
     */
    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();
        return Result.success(flashSaleService.findByShopId(shopId));
    }

    /**
     * 创建秒杀活动
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody FlashSale flashSale, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();

        flashSale.setShopId(shopId);
        flashSale.setSold(0);
        flashSale.setStatus(1);
        flashSaleService.save(flashSale);
        return Result.success("创建成功", flashSale);
    }

    /**
     * 更新秒杀活动
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody FlashSale flashSale) {
        flashSale.setId(id);
        flashSaleService.updateById(flashSale);
        return Result.success("更新成功");
    }

    /**
     * 删除秒杀活动
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        flashSaleService.removeById(id);
        return Result.success("删除成功");
    }
}
