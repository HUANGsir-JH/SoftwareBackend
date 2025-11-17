package org.software.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.content.tag.ContentTag;
import org.springframework.stereotype.Repository;

@Mapper
public interface ContentTagMapper extends BaseMapper<ContentTag> {
}
