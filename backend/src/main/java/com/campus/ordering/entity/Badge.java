package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 徽章定义表
 */
@Data
@TableName("badge")
public class Badge {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 徽章名称 */
    private String name;

    /** 徽章描述 */
    private String description;

    /** 徽章图标（emoji或图片URL） */
    private String icon;

    /** 徽章类型: 1-订单类, 2-消费类, 3-时间类, 4-特殊类 */
    private Integer type;

    /** 获取条件类型: order_count, total_spent, category_count, first_order, etc */
    private String conditionType;

    /** 获取条件值 */
    private Integer conditionValue;

    /** 稀有度: 1-普通, 2-稀有, 3-史诗, 4-传说 */
    private Integer rarity;

    /** 排序 */
    private Integer sortOrder;

    /** 状态: 0-禁用, 1-启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
