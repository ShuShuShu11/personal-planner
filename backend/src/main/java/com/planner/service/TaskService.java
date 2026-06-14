package com.planner.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planner.common.BusinessException;
import com.planner.common.PageResult;
import com.planner.dto.request.TaskCreateRequest;
import com.planner.dto.request.TaskUpdateRequest;
import com.planner.entity.SubTask;
import com.planner.entity.Task;
import com.planner.mapper.SubTaskMapper;
import com.planner.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final SubTaskMapper subTaskMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Task> getTodayTasks(Long userId) {
        LocalDate today = LocalDate.now();
        return taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getPlannedDate, today)
                .orderByAsc(Task::getPriority, Task::getCreatedAt)
        );
    }

    public PageResult<Task> getTasks(Long userId, String status, String priority, Long tagId, LocalDate start, LocalDate end, int page, int size) {
        Page<Task> p = taskMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(status != null, Task::getStatus, status)
                .eq(priority != null, Task::getPriority, priority)
                .ge(start != null, Task::getPlannedDate, start)
                .le(end != null, Task::getPlannedDate, end)
                .orderByDesc(Task::getCreatedAt)
        );
        return PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords());
    }

    public Task getTask(Long userId, Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new BusinessException("任务不存在");
        }
        return task;
    }

    @Transactional
    public Task createTask(Long userId, TaskCreateRequest req) {
        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setPriority(req.getPriority() != null ? req.getPriority() : "medium");
        task.setStatus("todo");
        task.setPlannedMinutes(req.getPlannedMinutes());
        task.setDueAt(req.getDueAt());
        task.setPlannedDate(req.getPlannedDate() != null ? req.getPlannedDate() : LocalDate.now());
        task.setTags(serializeTags(req.getTags()));
        taskMapper.insert(task);
        if (req.getSubTaskTitles() != null) {
            for (String title : req.getSubTaskTitles()) {
                SubTask st = new SubTask();
                st.setTaskId(task.getId());
                st.setTitle(title);
                st.setDone(false);
                subTaskMapper.insert(st);
            }
        }
        return task;
    }

    @Transactional
    public Task updateTask(Long userId, Long taskId, TaskUpdateRequest req) {
        Task task = getTask(userId, taskId);
        if (req.getTitle() != null) task.setTitle(req.getTitle());
        if (req.getDescription() != null) task.setDescription(req.getDescription());
        if (req.getStatus() != null) task.setStatus(req.getStatus());
        if (req.getPriority() != null) task.setPriority(req.getPriority());
        if (req.getPlannedMinutes() != null) task.setPlannedMinutes(req.getPlannedMinutes());
        if (req.getDueAt() != null) task.setDueAt(req.getDueAt());
        if (req.getPlannedDate() != null) task.setPlannedDate(req.getPlannedDate());
        if (req.getTags() != null) task.setTags(serializeTags(req.getTags()));
        taskMapper.updateById(task);
        return task;
    }

    @Transactional
    public void startTask(Long userId, Long taskId) {
        Task task = getTask(userId, taskId);
        if (!"todo".equals(task.getStatus())) {
            throw new BusinessException("只有待办状态才能开始");
        }
        task.setStatus("doing");
        task.setStartAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    @Transactional
    public void finishTask(Long userId, Long taskId) {
        Task task = getTask(userId, taskId);
        if ("done".equals(task.getStatus()) || "abandoned".equals(task.getStatus())) {
            throw new BusinessException("当前状态无法完成");
        }
        task.setStatus("done");
        task.setFinishAt(LocalDateTime.now());
        if (task.getStartAt() != null) {
            long minutes = Duration.between(task.getStartAt(), task.getFinishAt()).toMinutes();
            task.setActualMinutes((int) minutes);
        }
        taskMapper.updateById(task);
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = getTask(userId, taskId);
        taskMapper.deleteById(taskId);
        subTaskMapper.delete(new LambdaQueryWrapper<SubTask>().eq(SubTask::getTaskId, taskId));
    }

    @Transactional
    public void changeStatus(Long userId, Long taskId, String status) {
        if (status == null || status.isBlank()) throw new BusinessException("状态不能为空");
        Task task = getTask(userId, taskId);
        String old = task.getStatus();
        if (status.equals(old)) return;
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Task> uw =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        uw.eq("id", taskId).eq("user_id", userId).set("status", status);
        switch (status) {
            case "todo" -> uw.set("start_at", null).set("finish_at", null).set("actual_minutes", null);
            case "doing" -> {
                if (task.getStartAt() == null) uw.set("start_at", LocalDateTime.now());
                uw.set("finish_at", null).set("actual_minutes", null);
            }
            case "done" -> {
                LocalDateTime finishAt = task.getFinishAt() != null ? task.getFinishAt() : LocalDateTime.now();
                uw.set("finish_at", finishAt);
                if (task.getStartAt() != null) {
                    long minutes = Duration.between(task.getStartAt(), finishAt).toMinutes();
                    uw.set("actual_minutes", (int) Math.max(0, minutes));
                }
            }
            case "abandoned" -> {
                if (task.getFinishAt() == null) uw.set("finish_at", LocalDateTime.now());
            }
            default -> throw new BusinessException("无效状态: " + status);
        }
        taskMapper.update(null, uw);
    }

    public List<SubTask> getSubTasks(Long taskId) {
        return subTaskMapper.selectList(new LambdaQueryWrapper<SubTask>().eq(SubTask::getTaskId, taskId));
    }

    @Transactional
    public void updateSubTaskDone(Long userId, Long subTaskId, boolean done) {
        SubTask st = subTaskMapper.selectById(subTaskId);
        if (st == null) throw new BusinessException("子任务不存在");
        Task task = getTask(userId, st.getTaskId());
        st.setDone(done);
        subTaskMapper.updateById(st);
    }

    public List<String> parseTags(Task task) {
        if (task == null || task.getTags() == null || task.getTags().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(task.getTags(), new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String serializeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (Exception e) {
            return null;
        }
    }
}
