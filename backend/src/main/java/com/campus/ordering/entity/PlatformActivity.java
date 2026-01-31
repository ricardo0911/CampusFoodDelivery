package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("platform_activity")
public class PlatformActivity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String banner;

    /** 类型: 1-满减活动, 2-折扣活动, 3-免配送费 */
    private Integer type;

    private String rules;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /** 状态: 0-草稿, 1-进行中, 2-已结束 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
