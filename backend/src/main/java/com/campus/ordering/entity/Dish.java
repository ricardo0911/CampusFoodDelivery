package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private Long categoryId;
    private String name;
    private String image;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer stockWarning;
    private Integer sales;

    /** 是否特价: 0-否, 1-是 */
    private Integer isSpecial;

    /** 状态: 0-下架, 1-上架, 2-待审核 */
    private Integer status;

    /** 审核状态: 0-待审核, 1-通过, 2-驳回 */
    private Integer auditStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
