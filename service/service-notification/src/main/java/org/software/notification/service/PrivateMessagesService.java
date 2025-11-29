package org.software.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.social.SendMessageRequest;
import org.software.model.social.priv.PrivateMessages;


/**
 * 私聊消息表(PrivateMessages)表服务接口
 *
 * @author Ra1nbot
 * @since 2025-11-27 19:28:19
 */
public interface PrivateMessagesService extends IService<PrivateMessages> {

    PageResult getPrivateMessageDetail(PageQuery query, Long conversationId) throws Exception;

    void sendPrivateMessage(SendMessageRequest request) throws BusinessException;
}

