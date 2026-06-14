package com.planner.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("profile_document")
public class ProfileDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String markdown;
    private String sourceMd;
    private String suggestedSkills;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
