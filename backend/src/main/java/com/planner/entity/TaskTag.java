package com.planner.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskTag {
    private Long taskId;
    private Long tagId;
}