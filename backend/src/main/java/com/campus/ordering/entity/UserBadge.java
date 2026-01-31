package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户徽章表
 */
@Data
@TableName("user_badge")
public class UserBadge {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 徽章ID */
    private Long badgeId;

    /** 获得时间 */
    private LocalDateTime obtainedAt;

    /** 是否展示: 0-不展示, 1-展示 */
    private Integer displayed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
