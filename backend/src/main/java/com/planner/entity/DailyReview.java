package com.planner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_review")
public class DailyReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate reviewDate;
    private String doneSummary;
    private String undoneSummary;
    private String gains;
    private String improvements;
    private Integer moodScore;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}