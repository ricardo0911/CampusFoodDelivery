package com.campus.ordering.controller.customer;

import com.campus.ordering.dto.ReservationDTO;
import com.campus.ordering.dto.TimeslotDTO;
import com.campus.ordering.entity.PickupTimeslot;
import com.campus.ordering.entity.Reservation;
import com.campus.ordering.service.PickupTimeslotService;
import com.campus.ordering.service.ReservationService;
import com.campus.ordering.util.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预约取餐接口 - 顾客端
 */
@RestController
@RequestMapping("/api/customer/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final PickupTimeslotService timeslotService;

    /**
     * 获取店铺可用时段
     */
    @GetMapping("/timeslots/{shopId}")
    public Result<List<TimeslotDTO>> getTimeslots(
            @PathVariable Long shopId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<PickupTimeslot> timeslots = timeslotService.getAvailableTimeslots(shopId, date);
        List<TimeslotDTO> dtos = timeslots.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtos);
    }

    /**
     * 获取店铺未来几天的所有时段
     */
    @GetMapping("/timeslots/{shopId}/all")
    public Result<List<TimeslotDTO>> getAllTimeslots(
            @PathVariable Long shopId,
            @RequestParam(defaultValue = "3") int days) {
        List<PickupTimeslot> timeslots = timeslotService.getTimeslotsForDays(shopId, days);
        List<TimeslotDTO> dtos = timeslots.stream().map(this::convertToDTO).collect(Collectors.toList());
        return Result.success(dtos);
    }

    /**
     * 创建预约
     */
    @PostMapping
    public Result<ReservationDTO> createReservation(@RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationService.createReservation(request.getOrderId(), request.getTimeslotId());
        return Result.success(convertToReservationDTO(reservation));
    }

    /**
     * 获取订单的预约信息
     */
    @GetMapping("/order/{orderId}")
    public Result<ReservationDTO> getByOrderId(@PathVariable Long orderId) {
        Reservation reservation = reservationService.getByOrderId(orderId);
        if (reservation == null) {
            return Result.error("该订单无预约信息");
        }
        return Result.success(convertToReservationDTO(reservation));
    }

    /**
     * 取消预约
     */
    @DeleteMapping("/order/{orderId}")
    public Result<Void> cancelReservation(@PathVariable Long orderId) {
        if (reservationService.cancelReservation(orderId)) {
            return Result.success(null);
        }
        return Result.error("取消预约失败");
    }

    private TimeslotDTO convertToDTO(PickupTimeslot slot) {
        TimeslotDTO dto = new TimeslotDTO();
        dto.setId(slot.getId());
        dto.setDate(slot.getDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setTimeRange(slot.getTimeRange());
        dto.setRemaining(slot.getCapacity() - slot.getBookedCount());
        dto.setDiscountRate(slot.getDiscountRate());
        dto.setPeakLevel(slot.getPeakLevel());
        dto.setAvailable(slot.isAvailable());

        // 设置折扣描述
        if (slot.getDiscountRate().compareTo(BigDecimal.ONE) < 0) {
            int discount = slot.getDiscountRate().multiply(BigDecimal.valueOf(100)).intValue();
            dto.setDiscountText(discount + "折");
        } else {
            dto.setDiscountText("无折扣");
        }

        // 设置繁忙等级描述
        switch (slot.getPeakLevel()) {
            case 1:
                dto.setPeakLevelText("空闲");
                break;
            case 2:
                dto.setPeakLevelText("适中");
                break;
            case 3:
                dto.setPeakLevelText("繁忙");
                break;
            default:
                dto.setPeakLevelText("未知");
        }

        return dto;
    }

    private ReservationDTO convertToReservationDTO(Reservation r) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(r.getId());
        dto.setOrderId(r.getOrderId());
        dto.setPickupCode(r.getPickupCode());
        dto.setPickupDate(r.getPickupDate());
        dto.setPickupTimeRange(r.getPickupStartTime() + "-" + r.getPickupEndTime());
        dto.setEstimatedReadyTime(r.getEstimatedReadyTime());
        dto.setActualReadyTime(r.getActualReadyTime());
        dto.setStatus(r.getStatus());
        dto.setStatusText(ReservationDTO.getStatusText(r.getStatus()));
        dto.setDiscountAmount(r.getDiscountAmount());
        return dto;
    }

    @Data
    public static class CreateReservationRequest {
        private Long orderId;
        private Long timeslotId;
    }
}
