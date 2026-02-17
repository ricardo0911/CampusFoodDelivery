package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Order;
import com.campus.ordering.entity.PickupTimeslot;
import com.campus.ordering.entity.Reservation;
import com.campus.ordering.mapper.ReservationMapper;
import com.campus.ordering.service.OrderService;
import com.campus.ordering.service.PickupTimeslotService;
import com.campus.ordering.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    private final PickupTimeslotService timeslotService;
    private final OrderService orderService;
    private final Random random = new Random();

    @Override
    @Transactional
    public Reservation createReservation(Long orderId, Long timeslotId) {
        // 检查订单是否存在
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 检查是否已有预约
        if (baseMapper.selectByOrderId(orderId) != null) {
            throw new RuntimeException("该订单已有预约");
        }

        // 尝试预约时段
        if (!timeslotService.bookTimeslot(timeslotId)) {
            throw new RuntimeException("该时段已满，请选择其他时段");
        }

        // 获取时段信息
        PickupTimeslot timeslot = timeslotService.getById(timeslotId);

        // 创建预约
        Reservation reservation = new Reservation();
        reservation.setOrderId(orderId);
        reservation.setTimeslotId(timeslotId);
        reservation.setPickupCode(generatePickupCode());
        reservation.setPickupDate(timeslot.getDate());
        reservation.setPickupStartTime(timeslot.getStartTime());
        reservation.setPickupEndTime(timeslot.getEndTime());
        reservation.setStatus(Reservation.STATUS_PENDING);
        reservation.setReminderSent(0);
        reservation.setTimeoutHandled(0);

        // 计算错峰优惠
        if (timeslot.getDiscountRate().compareTo(BigDecimal.ONE) < 0) {
            BigDecimal discount = order.getTotalAmount()
                    .multiply(BigDecimal.ONE.subtract(timeslot.getDiscountRate()));
            reservation.setDiscountAmount(discount);
        } else {
            reservation.setDiscountAmount(BigDecimal.ZERO);
        }

        save(reservation);
        log.info("Created reservation for order {}, pickup code: {}", orderId, reservation.getPickupCode());
        return reservation;
    }

    @Override
    public Reservation getByOrderId(Long orderId) {
        return baseMapper.selectByOrderId(orderId);
    }

    @Override
    public Reservation verifyPickupCode(String pickupCode, LocalDate date) {
        return baseMapper.selectByPickupCodeAndDate(pickupCode, date);
    }

    @Override
    @Transactional
    public boolean markAsCooking(Long orderId) {
        Reservation reservation = getByOrderId(orderId);
        if (reservation == null)
            return false;

        reservation.setStatus(Reservation.STATUS_COOKING);
        return updateById(reservation);
    }

    @Override
    @Transactional
    public boolean markAsReady(Long orderId) {
        Reservation reservation = getByOrderId(orderId);
        if (reservation == null)
            return false;

        reservation.setStatus(Reservation.STATUS_READY);
        reservation.setActualReadyTime(LocalDateTime.now());
        return updateById(reservation);
    }

    @Override
    @Transactional
    public boolean markAsPickedUp(Long orderId) {
        Reservation reservation = getByOrderId(orderId);
        if (reservation == null)
            return false;

        reservation.setStatus(Reservation.STATUS_PICKED);
        reservation.setPickupTime(LocalDateTime.now());
        return updateById(reservation);
    }

    @Override
    @Transactional
    public boolean cancelReservation(Long orderId) {
        Reservation reservation = getByOrderId(orderId);
        if (reservation == null)
            return false;

        // 归还时段配额
        timeslotService.cancelBooking(reservation.getTimeslotId());

        reservation.setStatus(Reservation.STATUS_CANCELLED);
        return updateById(reservation);
    }

    @Override
    @Transactional
    public void handleTimeoutReservations() {
        // 默认30分钟超时
        List<Reservation> timeouts = baseMapper.selectTimeoutReservations(30);
        for (Reservation reservation : timeouts) {
            reservation.setStatus(Reservation.STATUS_TIMEOUT);
            reservation.setTimeoutHandled(1);
            updateById(reservation);

            // TODO: 发送超时通知给用户
            // TODO: 触发退款流程
            log.info("Reservation {} marked as timeout", reservation.getId());
        }
    }

    @Override
    @Transactional
    public void sendPickupReminders() {
        List<Reservation> readyReservations = baseMapper.selectReadyToRemind();
        for (Reservation reservation : readyReservations) {
            // TODO: 通过WebSocket发送提醒
            // websocketService.sendToUser(userId, "您的餐品已做好，取餐码：" +
            // reservation.getPickupCode());

            reservation.setReminderSent(1);
            updateById(reservation);
            log.info("Sent pickup reminder for reservation {}", reservation.getId());
        }
    }

    @Override
    public String generatePickupCode() {
        // 生成6位数字取餐码
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
