package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mystery_box")
public class MysteryBox {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 类型: 1-早餐, 2-午餐, 3-下午茶, 4-晚餐 */
    private Integer type;

    private BigDecimal price;
    private BigDecimal originalPrice;
    private String description;
    private Long shopId;
    private Integer dishCount;

    /** 状态: 0-下架, 1-上架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String shopName;
}
