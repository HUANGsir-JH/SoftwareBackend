package org.software.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.interaction.comment.Comments;
import org.springframework.stereotype.Repository;

@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {
}