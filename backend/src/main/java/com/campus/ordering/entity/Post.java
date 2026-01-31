package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String content;
    private String images;
    private Long dishId;
    private Long shopId;
    private Long orderId;
    private Integer likes;
    private Integer comments;

    /** 状态: 0-已删除, 1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String userNickname;
    @TableField(exist = false)
    private String userAvatar;
    @TableField(exist = false)
    private String dishName;
    @TableField(exist = false)
    private String shopName;
    @TableField(exist = false)
    private Boolean liked;
}
