package com.campus.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ordering.entity.PickupTimeslot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PickupTimeslotMapper extends BaseMapper<PickupTimeslot> {

    @Select("SELECT * FROM pickup_timeslot WHERE shop_id = #{shopId} AND date = #{date} AND status = 1 ORDER BY start_time")
    List<PickupTimeslot> selectAvailableByShopAndDate(@Param("shopId") Long shopId, @Param("date") LocalDate date);

    @Update("UPDATE pickup_timeslot SET booked_count = booked_count + 1, status = CASE WHEN booked_count + 1 >= capacity THEN 2 ELSE status END WHERE id = #{id} AND booked_count < capacity")
    int incrementBookedCount(@Param("id") Long id);

    @Update("UPDATE pickup_timeslot SET booked_count = booked_count - 1, status = 1 WHERE id = #{id} AND booked_count > 0")
    int decrementBookedCount(@Param("id") Long id);
}
