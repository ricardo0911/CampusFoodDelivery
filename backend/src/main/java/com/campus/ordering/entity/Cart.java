package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long shopId;
    private Long dishId;
    private String dishName;
    private String dishImage;
    private String specifications;
    private Integer quantity;
    private BigDecimal unitPrice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
