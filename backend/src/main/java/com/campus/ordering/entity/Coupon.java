package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String name;

    /** 类型: 1-满减券, 2-折扣券 */
    private Integer type;

    private BigDecimal discountValue;
    private BigDecimal minAmount;
    private Integer totalCount;
    private Integer remainCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
