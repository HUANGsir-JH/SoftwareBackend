package org.software.notification.controller;

import org.software.model.Response;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.social.SendMessageRequest;
import org.software.notification.service.PrivateConversationsService;
import org.software.notification.service.PrivateMessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class NotificationController {

    @Autowired
    private PrivateMessagesService privateMessagesService;
    @Autowired
    private PrivateConversationsService privateConversationsService;

    /**
     * 获取私聊列表
     */
    @GetMapping("/private")
    public Response getPrivateChatList(PageQuery query) {
        return Response.success(privateConversationsService.getPrivateChatList(query));
    }

    /**
     * 发送私聊消息
     */
    @PostMapping("/private")
    public Response sendPrivateMessage(@RequestBody SendMessageRequest request) throws BusinessException {
        privateMessagesService.sendPrivateMessage(request);
        return Response.success();
    }

    /**
     * 获取指定私聊消息
     */
    @GetMapping("/private/view")
    public Response getPrivateMessageDetail(PageQuery query, Long conversationId) throws Exception {
        return Response.success(privateMessagesService.getPrivateMessageDetail(query, conversationId));
    }

    @PostMapping("/addConv")
    public Response addConv(@RequestBody Long friendId) {
        privateConversationsService.addConv(friendId);
        return Response.success();
    }
}
