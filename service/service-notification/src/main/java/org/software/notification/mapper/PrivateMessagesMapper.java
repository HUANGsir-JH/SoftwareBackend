package org.software.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.social.priv.PrivateMessages;


/**
 * 私聊消息表(PrivateMessages)表数据库访问层
 *
 * @author Ra1nbot
 * @since 2025-11-27 19:28:19
 */
@Mapper
public interface PrivateMessagesMapper extends BaseMapper<PrivateMessages> {

}

