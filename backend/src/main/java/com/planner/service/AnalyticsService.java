package com.planner.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.planner.entity.Task;
import com.planner.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TaskMapper taskMapper;

    public List<Map<String, Object>> getHeatmap(Long userId, int days) {
        LocalDate start = LocalDate.now().minusDays(days);
        List<Task> tasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .ge(Task::getPlannedDate, start)
                .le(Task::getPlannedDate, LocalDate.now()));
        Map<LocalDate, Integer> dateCount = new HashMap<>();
        for (Task t : tasks) {
            if ("done".equals(t.getStatus())) {
                dateCount.merge(t.getPlannedDate(), 1, Integer::sum);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Integer> e : dateCount.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("date", e.getKey().toString());
            m.put("count", e.getValue());
            result.add(m);
        }
        return result;
    }

    public Map<String, Object> getTimeByTag(Long userId, int days) {
        LocalDate start = LocalDate.now().minusDays(days);
        List<Task> tasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getStatus, "done")
                .ge(Task::getPlannedDate, start)
                .le(Task::getPlannedDate, LocalDate.now()));
        int totalMinutes = tasks.stream()
                .mapToInt(t -> t.getActualMinutes() != null ? t.getActualMinutes() : 0)
                .sum();
        Map<String, Object> result = new HashMap<>();
        result.put("totalMinutes", totalMinutes);
        result.put("taskCount", tasks.size());
        return result;
    }

    public List<Map<String, Object>> getCompletionTrend(Long userId, int days) {
        LocalDate start = LocalDate.now().minusDays(days);
        List<Task> allTasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .ge(Task::getPlannedDate, start)
                .le(Task::getPlannedDate, LocalDate.now()));
        Map<LocalDate, long[]> buckets = new HashMap<>();
        for (Task t : allTasks) {
            LocalDate d = t.getPlannedDate();
            if (d == null) continue;
            long[] bucket = buckets.computeIfAbsent(d, k -> new long[2]);
            bucket[0]++;
            if ("done".equals(t.getStatus())) {
                bucket[1]++;
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long[] bucket = buckets.getOrDefault(date, new long[2]);
            long total = bucket[0];
            long done = bucket[1];
            Map<String, Object> m = new HashMap<>();
            m.put("date", date.toString());
            m.put("total", total);
            m.put("done", done);
            m.put("rate", total > 0 ? (done * 100.0 / total) : 0);
            result.add(m);
        }
        return result;
    }

    /**
     * 今日总览：今日任务数 / 待办 / 进行 / 已完成 / 今日专注分钟
     */
    public Map<String, Object> getTodayOverview(Long userId) {
        LocalDate today = LocalDate.now();
        List<Task> todayTasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .eq(Task::getPlannedDate, today));
        long todoCount = todayTasks.stream().filter(t -> "todo".equals(t.getStatus())).count();
        long doingCount = todayTasks.stream().filter(t -> "doing".equals(t.getStatus())).count();
        long doneCount = todayTasks.stream().filter(t -> "done".equals(t.getStatus())).count();
        int todayMinutes = todayTasks.stream()
                .filter(t -> "done".equals(t.getStatus()))
                .mapToInt(t -> t.getActualMinutes() != null ? t.getActualMinutes() : 0)
                .sum();
        Map<String, Object> result = new HashMap<>();
        result.put("totalTasks", todayTasks.size());
        result.put("todoCount", todoCount);
        result.put("doingCount", doingCount);
        result.put("doneCount", doneCount);
        result.put("todayMinutes", todayMinutes);
        return result;
    }

    /**
     * 30 天总览：累计完成 / 累计任务数 / 任务专注分钟 / 完成率
     */
    public Map<String, Object> getRangeOverview(Long userId, int days) {
        LocalDate start = LocalDate.now().minusDays(days);
        List<Task> tasks = taskMapper.selectList(new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, userId)
                .ge(Task::getPlannedDate, start)
                .lt(Task::getPlannedDate, LocalDate.now().plusDays(1)));
        long total = tasks.size();
        long done = tasks.stream().filter(t -> "done".equals(t.getStatus())).count();
        int totalMinutes = tasks.stream()
                .filter(t -> "done".equals(t.getStatus()))
                .mapToInt(t -> t.getActualMinutes() != null ? t.getActualMinutes() : 0)
                .sum();
        Map<String, Object> result = new HashMap<>();
        result.put("totalTasks", total);
        result.put("doneTasks", done);
        result.put("completionRate", total > 0 ? Math.round(done * 100.0 / total) : 0);
        result.put("taskMinutes", totalMinutes);
        return result;
    }
}
