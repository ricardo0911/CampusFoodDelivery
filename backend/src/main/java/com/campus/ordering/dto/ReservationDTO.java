package com.campus.ordering.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约详情DTO
 */
@Data
public class ReservationDTO {
    private Long id;
    private Long orderId;
    private String orderNo;

    /** 取餐码 */
    private String pickupCode;

    /** 取餐日期 */
    private LocalDate pickupDate;

    /** 取餐时间范围 */
    private String pickupTimeRange;

    /** 预计完成时间 */
    private LocalDateTime estimatedReadyTime;

    /** 实际完成时间 */
    private LocalDateTime actualReadyTime;

    /** 状态码 */
    private Integer status;

    /** 状态描述 */
    private String statusText;

    /** 错峰优惠金额 */
    private BigDecimal discountAmount;

    /** 店铺名称 */
    private String shopName;

    /** 店铺ID */
    private Long shopId;

    /** 倒计时描述，如"预计5分钟后完成"、"已就绪，请尽快取餐" */
    private String countdownText;

    /** 是否超时预警 */
    private Boolean timeoutWarning;

    public static String getStatusText(int status) {
        switch (status) {
            case 0:
                return "待制作";
            case 1:
                return "制作中";
            case 2:
                return "已完成，请取餐";
            case 3:
                return "已取餐";
            case 4:
                return "超时未取";
            case 5:
                return "已取消";
            default:
                return "未知状态";
        }
    }
}
