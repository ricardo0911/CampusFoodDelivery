package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("flash_sale")
public class FlashSale {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dishId;
    private Long shopId;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer stock;
    private Integer sold;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** 状态: 0-未开始, 1-进行中, 2-已结束 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    // 非数据库字段，用于展示
    @TableField(exist = false)
    private String dishName;
    @TableField(exist = false)
    private String dishImage;
    @TableField(exist = false)
    private String shopName;
}
