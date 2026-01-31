package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("mystery_box_dish")
public class MysteryBoxDish {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long boxId;
    private Long dishId;

    /** 权重，用于随机选择 */
    private Integer weight;

    @TableField(exist = false)
    private String dishName;
    @TableField(exist = false)
    private String dishImage;
}
