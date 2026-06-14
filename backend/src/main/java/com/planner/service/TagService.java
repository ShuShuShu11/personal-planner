package com.planner.service;

import com.planner.common.BusinessException;
import com.planner.dto.request.TagRequest;
import com.planner.entity.Tag;
import com.planner.mapper.TagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagMapper tagMapper;

    public List<Tag> getTags(Long userId) {
        return tagMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, userId)
                .orderByAsc(Tag::getName));
    }

    public Tag createTag(Long userId, TagRequest req) {
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(req.getName());
        tag.setColor(req.getColor() != null ? req.getColor() : "#409EFF");
        tagMapper.insert(tag);
        return tag;
    }

    public Tag updateTag(Long userId, Long tagId, TagRequest req) {
        Tag tag = getTag(userId, tagId);
        tag.setName(req.getName());
        if (req.getColor() != null) tag.setColor(req.getColor());
        tagMapper.updateById(tag);
        return tag;
    }

    public void deleteTag(Long userId, Long tagId) {
        Tag tag = getTag(userId, tagId);
        tagMapper.deleteById(tagId);
    }

    private Tag getTag(Long userId, Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new BusinessException("标签不存在");
        }
        return tag;
    }
}