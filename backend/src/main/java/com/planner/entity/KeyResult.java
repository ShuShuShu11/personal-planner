package com.planner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("key_result")
public class KeyResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long goalId;
    private String title;
    private Double targetValue;
    private Double currentValue;
    private String unit;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}