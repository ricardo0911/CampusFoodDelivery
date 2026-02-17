package com.campus.ordering.job;

import com.campus.ordering.service.PickupTimeslotService;
import com.campus.ordering.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预约系统定时任务
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ReservationScheduledTasks {

    private final PickupTimeslotService timeslotService;
    private final ReservationService reservationService;

    /**
     * 每天凌晨1点自动生成未来几天的时段
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void generateTimeslots() {
        log.info("Starting scheduled timeslot generation...");
        try {
            timeslotService.autoGenerateTimeslots();
            log.info("Timeslot generation completed");
        } catch (Exception e) {
            log.error("Timeslot generation failed", e);
        }
    }

    /**
     * 每分钟检查超时预约
     */
    @Scheduled(fixedRate = 60000)
    public void checkTimeoutReservations() {
        try {
            reservationService.handleTimeoutReservations();
        } catch (Exception e) {
            log.error("Timeout check failed", e);
        }
    }

    /**
     * 每30秒检查并发送取餐提醒
     */
    @Scheduled(fixedRate = 30000)
    public void sendPickupReminders() {
        try {
            reservationService.sendPickupReminders();
        } catch (Exception e) {
            log.error("Reminder sending failed", e);
        }
    }
}
