package com.campus.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ordering.entity.ShopTimeslotConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShopTimeslotConfigMapper extends BaseMapper<ShopTimeslotConfig> {

    @Select("SELECT * FROM shop_timeslot_config WHERE shop_id = #{shopId}")
    ShopTimeslotConfig selectByShopId(@Param("shopId") Long shopId);
}
