package org.software.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.content.Content;


/**
 * 内容主表(Content)表数据库访问层
 *
 * @author Ra1nbot
 * @since 2025-11-11 09:40:54
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {

}

