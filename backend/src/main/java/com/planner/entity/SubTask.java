package com.planner.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sub_task")
public class SubTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String title;
    private Boolean done;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}