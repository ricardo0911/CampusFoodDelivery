package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约订单表
 */
@Data
@TableName("reservation")
public class Reservation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long timeslotId;

    /** 取餐码(6位数字) */
    private String pickupCode;

    /** 取餐日期 */
    private LocalDate pickupDate;

    /** 取餐开始时间 */
    private LocalTime pickupStartTime;

    /** 取餐结束时间 */
    private LocalTime pickupEndTime;

    /** 预计完成时间 */
    private LocalDateTime estimatedReadyTime;

    /** 实际完成时间 */
    private LocalDateTime actualReadyTime;

    /** 实际取餐时间 */
    private LocalDateTime pickupTime;

    /** 错峰优惠金额 */
    private BigDecimal discountAmount;

    /** 状态: 0-待制作 1-制作中 2-已完成待取 3-已取餐 4-超时未取 5-已取消 */
    private Integer status;

    /** 是否已发送提醒 */
    private Integer reminderSent;

    /** 超时是否已处理 */
    private Integer timeoutHandled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 状态常量
    public static final int STATUS_PENDING = 0;      // 待制作
    public static final int STATUS_COOKING = 1;      // 制作中
    public static final int STATUS_READY = 2;        // 已完成待取
    public static final int STATUS_PICKED = 3;       // 已取餐
    public static final int STATUS_TIMEOUT = 4;      // 超时未取
    public static final int STATUS_CANCELLED = 5;    // 已取消
}
