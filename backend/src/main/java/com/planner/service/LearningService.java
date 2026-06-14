package com.planner.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.planner.common.BusinessException;
import com.planner.common.PageResult;
import com.planner.dto.request.LearningRecordRequest;
import com.planner.entity.LearningRecord;
import com.planner.mapper.LearningRecordMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LearningService {

    private final LearningRecordMapper learningMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PageResult<LearningRecord> getRecords(Long userId, int page, int size,
                                                   String keyword, String source,
                                                   LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<LearningRecord> wrapper = new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(LearningRecord::getTitle, keyword)
                                .or().like(LearningRecord::getContent, keyword))
                .eq(source != null && !source.isBlank(), LearningRecord::getSource, source)
                .ge(start != null, LearningRecord::getCreatedAt, start)
                .le(end != null, LearningRecord::getCreatedAt, end)
                .orderByDesc(LearningRecord::getCreatedAt);
        Page<LearningRecord> p = learningMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(p.getTotal(), p.getCurrent(), p.getSize(), p.getRecords());
    }

    public LearningRecord getRecord(Long userId, Long id) {
        LearningRecord r = learningMapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) {
            throw new BusinessException("学习记录不存在");
        }
        return r;
    }

    public LearningRecord createRecord(Long userId, LearningRecordRequest req) {
        LearningRecord r = new LearningRecord();
        r.setUserId(userId);
        r.setTitle(req.getTitle());
        r.setContent(req.getContent());
        r.setDurationMinutes(req.getDurationMinutes());
        r.setSource(req.getSource());
        if (req.getTags() != null) {
            try {
                r.setTags(objectMapper.writeValueAsString(req.getTags()));
            } catch (Exception e) {
                r.setTags("");
            }
        }
        learningMapper.insert(r);
        return r;
    }

    public LearningRecord updateRecord(Long userId, Long id, LearningRecordRequest req) {
        LearningRecord r = getRecord(userId, id);
        if (req.getTitle() != null) r.setTitle(req.getTitle());
        if (req.getContent() != null) r.setContent(req.getContent());
        if (req.getDurationMinutes() != null) r.setDurationMinutes(req.getDurationMinutes());
        if (req.getSource() != null) r.setSource(req.getSource());
        if (req.getTags() != null) {
            try {
                r.setTags(objectMapper.writeValueAsString(req.getTags()));
            } catch (Exception ignored) {}
        }
        learningMapper.updateById(r);
        return r;
    }

    public void deleteRecord(Long userId, Long id) {
        getRecord(userId, id);
        learningMapper.deleteById(id);
    }

    public Map<String, Object> getStats(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<LearningRecord> recent = learningMapper.selectList(
                new LambdaQueryWrapper<LearningRecord>()
                        .eq(LearningRecord::getUserId, userId)
                        .ge(LearningRecord::getCreatedAt, thirtyDaysAgo));
        int totalMinutes = recent.stream().mapToInt(r -> r.getDurationMinutes() != null ? r.getDurationMinutes() : 0).sum();
        Map<String, Integer> tagDistribution = new HashMap<>();
        for (LearningRecord r : recent) {
            if (r.getTags() != null && !r.getTags().isEmpty()) {
                try {
                    List<String> tags = objectMapper.readValue(r.getTags(), new TypeReference<List<String>>() {});
                    for (String tag : tags) {
                        tagDistribution.merge(tag, 1, Integer::sum);
                    }
                } catch (Exception ignored) {}
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("recentCount", recent.size());
        result.put("recentMinutes", totalMinutes);
        result.put("tagDistribution", tagDistribution);
        return result;
    }

    public List<LearningRecord> getRecordsSince(Long userId, LocalDateTime since) {
        return learningMapper.selectList(new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getUserId, userId)
                .ge(LearningRecord::getCreatedAt, since)
                .orderByAsc(LearningRecord::getCreatedAt));
    }
}