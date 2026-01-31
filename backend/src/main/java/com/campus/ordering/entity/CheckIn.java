package com.campus.ordering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("check_in")
public class CheckIn {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private LocalDate checkInDate;
    private Integer pointsEarned;
    private Integer continuousDays;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
