package org.software.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.social.priv.PrivateConversations;


/**
 * 私聊会话表(PrivateConversations)表数据库访问层
 *
 * @author Ra1nbot
 * @since 2025-11-27 22:06:51
 */
@Mapper
public interface PrivateConversationsMapper extends BaseMapper<PrivateConversations> {

    Page<PrivateConversations> pageC(Page<PrivateConversations> page, Long userId);
}

