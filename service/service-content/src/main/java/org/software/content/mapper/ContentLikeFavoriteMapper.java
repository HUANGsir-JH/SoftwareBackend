package org.software.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.interaction.ContentLikeFavorite;
import org.springframework.stereotype.Repository;

@Mapper
public interface ContentLikeFavoriteMapper extends BaseMapper<ContentLikeFavorite> {
}