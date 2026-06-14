package com.planner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.planner.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}