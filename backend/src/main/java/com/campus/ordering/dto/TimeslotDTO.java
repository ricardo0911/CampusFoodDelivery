package com.campus.ordering.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约时段DTO
 */
@Data
public class TimeslotDTO {
    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String timeRange; // "11:30-11:45"

    /** 剩余可预约数 */
    private Integer remaining;

    /** 折扣率 */
    private BigDecimal discountRate;

    /** 折扣描述，如"85折"、"无折扣" */
    private String discountText;

    /** 繁忙等级: 1-低峰 2-平峰 3-高峰 */
    private Integer peakLevel;

    /** 繁忙等级描述 */
    private String peakLevelText;

    /** 是否可预约 */
    private Boolean available;
}
