package com.planner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Integer plannedMinutes;
    private Integer actualMinutes;
    private LocalDateTime startAt;
    private LocalDateTime finishAt;
    private LocalDate dueAt;
    private LocalDate plannedDate;
    private String tags;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
