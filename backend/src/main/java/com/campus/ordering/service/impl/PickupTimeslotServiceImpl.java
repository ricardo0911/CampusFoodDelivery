package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.PickupTimeslot;
import com.campus.ordering.entity.ShopTimeslotConfig;
import com.campus.ordering.mapper.PickupTimeslotMapper;
import com.campus.ordering.mapper.ShopTimeslotConfigMapper;
import com.campus.ordering.service.PickupTimeslotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PickupTimeslotServiceImpl extends ServiceImpl<PickupTimeslotMapper, PickupTimeslot>
        implements PickupTimeslotService {

    private final ShopTimeslotConfigMapper configMapper;

    @Override
    public List<PickupTimeslot> getAvailableTimeslots(Long shopId, LocalDate date) {
        return baseMapper.selectAvailableByShopAndDate(shopId, date);
    }

    @Override
    public List<PickupTimeslot> getTimeslotsForDays(Long shopId, int days) {
        List<PickupTimeslot> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            result.addAll(getAvailableTimeslots(shopId, today.plusDays(i)));
        }
        return result;
    }

    @Override
    @Transactional
    public boolean bookTimeslot(Long timeslotId) {
        int rows = baseMapper.incrementBookedCount(timeslotId);
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean cancelBooking(Long timeslotId) {
        int rows = baseMapper.decrementBookedCount(timeslotId);
        return rows > 0;
    }

    @Override
    @Transactional
    public void generateTimeslotsForDate(Long shopId, LocalDate date) {
        // 检查是否已生成
        LambdaQueryWrapper<PickupTimeslot> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(PickupTimeslot::getShopId, shopId).eq(PickupTimeslot::getDate, date);
        if (count(existQuery) > 0) {
            log.debug("Timeslots already exist for shop {} on {}", shopId, date);
            return;
        }

        // 获取店铺配置
        ShopTimeslotConfig config = configMapper.selectByShopId(shopId);
        if (config == null || config.getEnabled() != 1) {
            log.debug("Timeslot generation disabled for shop {}", shopId);
            return;
        }

        // 定义营业时间（可以从Shop表获取，这里简化处理）
        List<TimeRange> businessHours = List.of(
                new TimeRange(LocalTime.of(7, 0), LocalTime.of(14, 0)),
                new TimeRange(LocalTime.of(17, 0), LocalTime.of(21, 0)));

        int slotDuration = config.getSlotDuration();
        int capacity = config.getDefaultCapacity();

        List<PickupTimeslot> slots = new ArrayList<>();
        for (TimeRange range : businessHours) {
            LocalTime current = range.start;
            while (current.plusMinutes(slotDuration).isBefore(range.end) ||
                    current.plusMinutes(slotDuration).equals(range.end)) {

                PickupTimeslot slot = new PickupTimeslot();
                slot.setShopId(shopId);
                slot.setDate(date);
                slot.setStartTime(current);
                slot.setEndTime(current.plusMinutes(slotDuration));
                slot.setCapacity(capacity);
                slot.setBookedCount(0);
                slot.setStatus(1);

                // 计算繁忙等级和折扣
                int peakLevel = calculatePeakLevel(current, config);
                slot.setPeakLevel(peakLevel);
                slot.setDiscountRate(getDiscountRate(peakLevel, config));

                slots.add(slot);
                current = current.plusMinutes(slotDuration);
            }
        }

        if (!slots.isEmpty()) {
            saveBatch(slots);
            log.info("Generated {} timeslots for shop {} on {}", slots.size(), shopId, date);
        }
    }

    private int calculatePeakLevel(LocalTime time, ShopTimeslotConfig config) {
        if (config.getPeakHours() != null) {
            for (ShopTimeslotConfig.PeakHour peak : config.getPeakHours()) {
                LocalTime start = LocalTime.parse(peak.getStart());
                LocalTime end = LocalTime.parse(peak.getEnd());
                if (!time.isBefore(start) && time.isBefore(end)) {
                    return 3; // 高峰
                }
                // 高峰前后30分钟为平峰
                if (!time.isBefore(start.minusMinutes(30)) && time.isBefore(start)) {
                    return 2;
                }
                if (!time.isBefore(end) && time.isBefore(end.plusMinutes(30))) {
                    return 2;
                }
            }
        }
        return 1; // 低峰
    }

    private BigDecimal getDiscountRate(int peakLevel, ShopTimeslotConfig config) {
        switch (peakLevel) {
            case 1:
                return config.getOffPeakDiscount();
            case 2:
                return config.getNormalDiscount();
            default:
                return BigDecimal.ONE;
        }
    }

    @Override
    @Transactional
    public void autoGenerateTimeslots() {
        // 获取所有启用预约的店铺配置
        LambdaQueryWrapper<ShopTimeslotConfig> query = new LambdaQueryWrapper<>();
        query.eq(ShopTimeslotConfig::getEnabled, 1).eq(ShopTimeslotConfig::getAutoGenerate, 1);
        List<ShopTimeslotConfig> configs = configMapper.selectList(query);

        LocalDate today = LocalDate.now();
        for (ShopTimeslotConfig config : configs) {
            int advanceDays = config.getAdvanceDays() != null ? config.getAdvanceDays() : 3;
            for (int i = 0; i <= advanceDays; i++) {
                generateTimeslotsForDate(config.getShopId(), today.plusDays(i));
            }
        }
    }

    private static class TimeRange {
        LocalTime start;
        LocalTime end;

        TimeRange(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
    }
}
