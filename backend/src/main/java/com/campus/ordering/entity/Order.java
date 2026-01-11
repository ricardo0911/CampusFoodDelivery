package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private Long shopId;
    private String shopName;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Long addressId;
    private String addressDetail;
    private String contactName;
    private String contactPhone;
    private String remark;
    private Long couponId;

    /** 状态: 0-待支付, 1-待接单, 2-制作中, 3-配送中, 4-已完成, 5-已取消, 6-退款中, 7-已退款 */
    private Integer status;

    private LocalDateTime payTime;
    private LocalDateTime acceptTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
    private String cancelReason;
    private Integer estimatedTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
