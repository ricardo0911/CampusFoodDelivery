package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("flash_sale_order")
public class FlashSaleOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flashSaleId;
    private Long userId;
    private Long orderId;

    /** 状态: 0-待支付, 1-已支付, 2-已取消 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
