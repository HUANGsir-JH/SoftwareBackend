package org.software.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.social.UnreadCounts;

import java.util.List;


/**
 * 会话未读消息数记录表(UnreadCounts)表数据库访问层
 *
 * @author Ra1nbot
 * @since 2025-11-29 21:15:28
 */
@Mapper
public interface UnreadCountsMapper extends BaseMapper<UnreadCounts> {

    void batchInsert(List<UnreadCounts> list);
}

