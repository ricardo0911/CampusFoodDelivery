package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("marketing_campaign")
public class MarketingCampaign {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long shopId;
    private String name;

    /** 类型: 1-流失召回, 2-新品推广, 3-节日活动 */
    private Integer type;

    private String targetUsers;
    private Long couponId;
    private String message;

    /** 状态: 0-草稿, 1-待发送, 2-已发送 */
    private Integer status;

    private LocalDateTime sendTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
