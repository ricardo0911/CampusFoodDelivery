package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 店铺时段配置表
 */
@Data
@TableName(value = "shop_timeslot_config", autoResultMap = true)
public class ShopTimeslotConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;

    /** 是否启用预约功能 */
    private Integer enabled;

    /** 时段间隔(分钟) */
    private Integer slotDuration;

    /** 默认每时段容量 */
    private Integer defaultCapacity;

    /** 高峰时段配置 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<PeakHour> peakHours;

    /** 低峰折扣率 */
    private BigDecimal offPeakDiscount;

    /** 平峰折扣率 */
    private BigDecimal normalDiscount;

    /** 可提前预约天数 */
    private Integer advanceDays;

    /** 取餐超时时间(分钟) */
    private Integer pickupTimeoutMinutes;

    /** 是否自动生成时段 */
    private Integer autoGenerate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 高峰时段配置
     */
    @Data
    public static class PeakHour {
        private String start;
        private String end;
    }
}
