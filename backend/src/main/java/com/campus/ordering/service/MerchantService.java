package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Merchant;

public interface MerchantService extends IService<Merchant> {
    Merchant findByUsername(String username);
}
