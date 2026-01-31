package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content;

    /** 类型: 1-系统公告, 2-活动通知, 3-维护通知 */
    private Integer type;

    /** 目标: 0-全部, 1-用户, 2-商家 */
    private Integer target;

    private Integer isPopup;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** 状态: 0-草稿, 1-已发布 */
    private Integer status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
