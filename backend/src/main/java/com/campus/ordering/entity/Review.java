package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long userId;
    private Long shopId;
    private Integer shopRating;
    private Integer deliveryRating;
    private String content;
    private String images;
    private String merchantReply;
    private LocalDateTime replyTime;

    /** 状态: 0-已删除, 1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
