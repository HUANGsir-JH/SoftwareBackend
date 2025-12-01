package org.software.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.interaction.InteractionRecord;

@Mapper
public interface InteractionRecordMapper extends BaseMapper<InteractionRecord> {
}
