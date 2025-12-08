package org.software.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.media.ContentMedia;


/**
 * 内容关联的媒体文件表(ContentMedia)表数据库访问层
 *
 * @author Ra1nbot
 * @since 2025-12-08 16:31:02
 */
@Mapper
public interface ContentMediaMapper extends BaseMapper<ContentMedia> {

}

