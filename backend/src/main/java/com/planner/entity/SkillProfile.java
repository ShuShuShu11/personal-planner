package com.planner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("skill_profile")
public class SkillProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String category;
    private String skillName;
    private String level;
    private String keywords;
    private String notes;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}