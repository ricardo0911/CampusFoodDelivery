package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼单主表
 */
@Data
@TableName("group_order")
public class GroupOrder {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 拼单号（6位分享码） */
    private String groupCode;

    /** 发起人ID */
    private Long creatorId;

    /** 发起人昵称 */
    private String creatorName;

    /** 店铺ID */
    private Long shopId;

    /** 店铺名称 */
    private String shopName;

    /** 拼单标题 */
    private String title;

    /** 最大人数 */
    private Integer maxMembers;

    /** 当前人数 */
    private Integer currentMembers;

    /** 目标金额（达到后享优惠） */
    private BigDecimal targetAmount;

    /** 当前金额 */
    private BigDecimal currentAmount;

    /** 优惠类型: 1-满减, 2-折扣, 3-免配送费 */
    private Integer discountType;

    /** 优惠值（满减金额或折扣比例） */
    private BigDecimal discountValue;

    /** 状态: 0-拼单中, 1-已成团, 2-已完成, 3-已取消, 4-已过期 */
    private Integer status;

    /** 过期时间 */
    private LocalDateTime expireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
