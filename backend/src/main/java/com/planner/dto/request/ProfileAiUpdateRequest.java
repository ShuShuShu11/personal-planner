package com.planner.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProfileAiUpdateRequest {
    private LocalDateTime since;
}