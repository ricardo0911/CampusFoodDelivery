package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约时段表
 */
@Data
@TableName("pickup_timeslot")
public class PickupTimeslot {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;

    /** 日期 */
    private LocalDate date;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 该时段最大订单数 */
    private Integer capacity;

    /** 已预约数量 */
    private Integer bookedCount;

    /** 折扣率 0.85表示85折 */
    private BigDecimal discountRate;

    /** 繁忙等级: 1-低峰(绿) 2-平峰(黄) 3-高峰(红) */
    private Integer peakLevel;

    /** 状态: 0-关闭 1-开放 2-已满 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 便捷方法
    public boolean isAvailable() {
        return status == 1 && bookedCount < capacity;
    }

    public String getTimeRange() {
        return startTime.toString() + "-" + endTime.toString();
    }
}
