package com.campus.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.ordering.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    @Select("SELECT * FROM reservation WHERE order_id = #{orderId}")
    Reservation selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT * FROM reservation WHERE pickup_code = #{pickupCode} AND pickup_date = #{date}")
    Reservation selectByPickupCodeAndDate(@Param("pickupCode") String pickupCode, @Param("date") LocalDate date);

    @Select("SELECT * FROM reservation WHERE status = 2 AND reminder_sent = 0 AND pickup_date = CURDATE()")
    List<Reservation> selectReadyToRemind();

    @Select("SELECT * FROM reservation WHERE status = 2 AND timeout_handled = 0 AND TIMESTAMPDIFF(MINUTE, actual_ready_time, NOW()) > #{timeoutMinutes}")
    List<Reservation> selectTimeoutReservations(@Param("timeoutMinutes") int timeoutMinutes);
}
