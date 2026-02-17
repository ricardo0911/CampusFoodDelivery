package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.PickupTimeslot;

import java.time.LocalDate;
import java.util.List;

public interface PickupTimeslotService extends IService<PickupTimeslot> {

    /**
     * 获取店铺指定日期的可用时段
     */
    List<PickupTimeslot> getAvailableTimeslots(Long shopId, LocalDate date);

    /**
     * 获取店铺未来N天的所有时段
     */
    List<PickupTimeslot> getTimeslotsForDays(Long shopId, int days);

    /**
     * 预约时段（原子操作增加预约数）
     */
    boolean bookTimeslot(Long timeslotId);

    /**
     * 取消预约（原子操作减少预约数）
     */
    boolean cancelBooking(Long timeslotId);

    /**
     * 为店铺生成指定日期的时段
     */
    void generateTimeslotsForDate(Long shopId, LocalDate date);

    /**
     * 自动生成所有店铺的未来时段（定时任务调用）
     */
    void autoGenerateTimeslots();
}
