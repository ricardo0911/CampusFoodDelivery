package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("complaint")
public class Complaint {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long userId;
    private Long merchantId;

    /** 类型: 1-用户投诉, 2-商家申诉 */
    private Integer type;

    private String reason;
    private String images;

    /** 状态: 0-待处理, 1-处理中, 2-已解决 */
    private Integer status;

    private String result;
    private Long handlerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String orderNo;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String shopName;
}
