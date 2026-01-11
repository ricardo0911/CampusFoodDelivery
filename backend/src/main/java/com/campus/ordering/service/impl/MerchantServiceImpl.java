package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Merchant;
import com.campus.ordering.mapper.MerchantMapper;
import com.campus.ordering.service.MerchantService;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements MerchantService {

    @Override
    public Merchant findByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getUsername, username));
    }
}
