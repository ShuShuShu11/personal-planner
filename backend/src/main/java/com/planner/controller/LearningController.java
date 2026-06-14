package com.planner.controller;

import com.planner.common.PageResult;
import com.planner.common.Result;
import com.planner.dto.request.LearningRecordRequest;
import com.planner.entity.LearningRecord;
import com.planner.service.LearningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/learnings")
@RequiredArgsConstructor
public class LearningController {

    private final LearningService learningService;

    @GetMapping
    public Result<PageResult<LearningRecord>> list(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        LocalDateTime startDt = start != null ? start.atStartOfDay() : null;
        LocalDateTime endDt = end != null ? end.atTime(23, 59, 59) : null;
        return Result.success(learningService.getRecords(
                getUserId(user.getUsername()), page, size, keyword, source, startDt, endDt));
    }

    @GetMapping("/{id}")
    public Result<LearningRecord> get(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        return Result.success(learningService.getRecord(getUserId(user.getUsername()), id));
    }

    @PostMapping
    public Result<LearningRecord> create(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody LearningRecordRequest request) {
        return Result.success(learningService.createRecord(getUserId(user.getUsername()), request));
    }

    @PutMapping("/{id}")
    public Result<LearningRecord> update(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody LearningRecordRequest request) {
        return Result.success(learningService.updateRecord(getUserId(user.getUsername()), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        learningService.deleteRecord(getUserId(user.getUsername()), id);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@AuthenticationPrincipal UserDetails user) {
        return Result.success(learningService.getStats(getUserId(user.getUsername())));
    }

    private Long getUserId(String username) {
        return 1L;
    }
}