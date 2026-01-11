package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shop")
public class Shop {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;
    private String name;
    private String logo;
    private String banner;
    private String description;
    private String announcement;
    private String businessHours;
    private String deliveryScope;
    private BigDecimal minOrderAmount;
    private BigDecimal deliveryFee;
    private BigDecimal rating;
    private Integer monthlySales;

    /** 状态: 0-休息中, 1-营业中 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
