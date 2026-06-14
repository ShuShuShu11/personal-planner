package com.planner.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank(message = "标签名不能为空")
    private String name;
    private String color;
}