package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.DishCategory;
import com.campus.ordering.mapper.DishCategoryMapper;
import com.campus.ordering.service.DishCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory>
        implements DishCategoryService {

    @Override
    public List<DishCategory> findByShopId(Long shopId) {
        return this.list(new LambdaQueryWrapper<DishCategory>()
                .eq(DishCategory::getShopId, shopId)
                .eq(DishCategory::getStatus, 1)
                .orderByAsc(DishCategory::getSortOrder));
    }
}
