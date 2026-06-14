package com.planner.controller;

import com.planner.common.PageResult;
import com.planner.common.Result;
import com.planner.dto.request.TaskCreateRequest;
import com.planner.dto.request.TaskUpdateRequest;
import com.planner.entity.SubTask;
import com.planner.entity.Task;
import com.planner.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/today")
    public Result<List<Task>> today(@AuthenticationPrincipal UserDetails user) {
        return Result.success(taskService.getTodayTasks(getUserId(user.getUsername())));
    }

    @GetMapping
    public Result<PageResult<Task>> list(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(taskService.getTasks(getUserId(user.getUsername()), status, priority, null, start, end, page, size));
    }

    @GetMapping("/{id}")
    public Result<Task> get(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        return Result.success(taskService.getTask(getUserId(user.getUsername()), id));
    }

    @PostMapping
    public Result<Task> create(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody TaskCreateRequest request) {
        return Result.success(taskService.createTask(getUserId(user.getUsername()), request));
    }

    @PutMapping("/{id}")
    public Result<Task> update(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody TaskUpdateRequest request) {
        return Result.success(taskService.updateTask(getUserId(user.getUsername()), id, request));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody StatusRequest request) {
        taskService.changeStatus(getUserId(user.getUsername()), id, request.getStatus());
        return Result.success();
    }

    @PutMapping("/{id}/start")
    public Result<Void> start(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        taskService.startTask(getUserId(user.getUsername()), id);
        return Result.success();
    }

    @PutMapping("/{id}/finish")
    public Result<Void> finish(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        taskService.finishTask(getUserId(user.getUsername()), id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        taskService.deleteTask(getUserId(user.getUsername()), id);
        return Result.success();
    }

    @GetMapping("/{id}/subtasks")
    public Result<List<SubTask>> subTasks(@PathVariable Long id) {
        return Result.success(taskService.getSubTasks(id));
    }

    @PutMapping("/subtasks/{subTaskId}")
    public Result<Void> updateSubTask(@AuthenticationPrincipal UserDetails user, @PathVariable Long subTaskId, @RequestBody SubTaskDoneRequest request) {
        taskService.updateSubTaskDone(getUserId(user.getUsername()), subTaskId, request.isDone());
        return Result.success();
    }

    private Long getUserId(String username) {
        return 1L;
    }

    @lombok.Data
    public static class StatusRequest {
        private String status;
    }

    @lombok.Data
    public static class SubTaskDoneRequest {
        private boolean done;
    }
}