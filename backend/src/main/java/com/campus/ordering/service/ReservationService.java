package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Reservation;

import java.time.LocalDate;

public interface ReservationService extends IService<Reservation> {

    /**
     * 创建预约
     */
    Reservation createReservation(Long orderId, Long timeslotId);

    /**
     * 根据订单ID获取预约信息
     */
    Reservation getByOrderId(Long orderId);

    /**
     * 验证取餐码
     */
    Reservation verifyPickupCode(String pickupCode, LocalDate date);

    /**
     * 标记为制作中
     */
    boolean markAsCooking(Long orderId);

    /**
     * 标记为已完成待取
     */
    boolean markAsReady(Long orderId);

    /**
     * 标记为已取餐
     */
    boolean markAsPickedUp(Long orderId);

    /**
     * 取消预约
     */
    boolean cancelReservation(Long orderId);

    /**
     * 处理超时预约（定时任务调用）
     */
    void handleTimeoutReservations();

    /**
     * 发送取餐提醒（定时任务调用）
     */
    void sendPickupReminders();

    /**
     * 生成取餐码
     */
    String generatePickupCode();
}
