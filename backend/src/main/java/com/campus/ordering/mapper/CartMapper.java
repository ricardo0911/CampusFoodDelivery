package com.campus.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ordering.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
