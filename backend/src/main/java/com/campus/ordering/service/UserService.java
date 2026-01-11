package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.User;

public interface UserService extends IService<User> {
    User findByUsername(String username);

    User findByPhone(String phone);
}
