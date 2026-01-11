package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Shop;

public interface ShopService extends IService<Shop> {
    Shop findByMerchantId(Long merchantId);
}
