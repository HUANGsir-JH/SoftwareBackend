package org.software.content.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.software.model.media.ContentMedia;

@Mapper
public interface ContentMediaMapper extends BaseMapper<ContentMedia> {
    int delete(@Param("ew") Wrapper<ContentMedia> wrapper);
}
