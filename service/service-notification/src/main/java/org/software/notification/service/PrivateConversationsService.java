package org.software.notification.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.social.priv.PrivateConversations;


/**
 * 私聊会话表(PrivateConversations)表服务接口
 *
 * @author Ra1nbot
 * @since 2025-11-27 22:06:51
 */
public interface PrivateConversationsService extends IService<PrivateConversations> {

    void addConv(Long friendId);

    PageResult getPrivateChatList(PageQuery query);

    void updateConv(PrivateConversations conv);
}

