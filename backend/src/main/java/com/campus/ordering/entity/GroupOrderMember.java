package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼单成员表
 */
@Data
@TableName("group_order_member")
public class GroupOrderMember {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 拼单ID */
    private Long groupOrderId;

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String nickname;

    /** 用户头像 */
    private String avatar;

    /** 购物车内容（JSON格式） */
    private String cartItems;

    /** 小计金额 */
    private BigDecimal subtotal;

    /** 是否已支付: 0-未支付, 1-已支付 */
    private Integer paid;

    /** 加入时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
