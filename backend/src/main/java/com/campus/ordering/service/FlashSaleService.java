package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.FlashSale;

import java.util.List;
import java.util.Map;

public interface FlashSaleService extends IService<FlashSale> {
    List<FlashSale> getCurrentFlashSales();
    List<FlashSale> getUpcomingFlashSales();
    Map<String, Object> grab(Long flashSaleId, Long userId);
    List<FlashSale> findByShopId(Long shopId);
}
