package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.mapper.ShopMapper;
import com.campus.ordering.service.ShopService;
import org.springframework.stereotype.Service;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements ShopService {

    @Override
    public Shop findByMerchantId(Long merchantId) {
        return this.getOne(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, merchantId));
    }
}
