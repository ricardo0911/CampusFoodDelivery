package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Address;

import java.util.List;

public interface AddressService extends IService<Address> {
    List<Address> findByUserId(Long userId);

    Address getDefaultAddress(Long userId);
}
