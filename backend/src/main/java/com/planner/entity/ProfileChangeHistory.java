package com.planner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("profile_change_history")
public class ProfileChangeHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String skillName;
    private String oldLevel;
    private String newLevel;
    private String changeType;
    private String reason;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
