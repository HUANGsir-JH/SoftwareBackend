package org.software.notification.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.http.config.MessageConstraints;
import org.checkerframework.checker.units.qual.A;
import org.software.common.util.RedisHelper;
import org.software.feign.UserFeignClient;
import org.software.model.Response;
import org.software.model.constants.FriendsConstants;
import org.software.model.constants.MessageConstants;
import org.software.model.constants.UserConstants;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.social.SendMessageRequest;
import org.software.model.social.priv.PrivateConversations;
import org.software.model.social.priv.PrivateMessages;
import org.software.model.user.User;
import org.software.model.user.UserV;
import org.software.notification.mapper.PrivateConversationsMapper;
import org.software.notification.mapper.PrivateMessagesMapper;
import org.software.notification.service.PrivateConversationsService;
import org.software.notification.service.PrivateMessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 私聊消息表(PrivateMessages)表服务实现类
 *
 * @author Ra1nbot
 * @since 2025-11-27 19:28:19
 */
@Service
public class PrivateMessagesServiceImpl extends ServiceImpl<PrivateMessagesMapper, PrivateMessages> implements PrivateMessagesService {

    @Autowired
    private PrivateConversationsService privateConversationsService;

    @Override
    public PageResult getPrivateMessageDetail(PageQuery query, Long conversationId) throws Exception {
        if (conversationId == null) {
            throw new RuntimeException("会话ID不能为空");
        }

        Page<PrivateMessages> page = new Page<>(query.getPageNum(), query.getPageSize());
        QueryWrapper<PrivateMessages> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId)
                .orderByDesc("created_time");
        page = page(page, queryWrapper);

        // 更新已读
        List<PrivateMessages> list = page.getRecords().stream()
                .peek(item -> item.setIsRead(MessageConstants.IS_READ)).toList();
        saveBatch(list);

        return PageResult.builder()
                .total(page.getTotal())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .records(page.getRecords())
                .build();
    }

    @Transactional
    @Override
    public void sendPrivateMessage(SendMessageRequest request) throws BusinessException {
        long userId = StpUtil.getLoginIdAsLong();

        String content = switch (request.getType()){
            case MessageConstants.TEXT -> request.getContent();
            case MessageConstants.IMAGE -> MessageConstants.IMAGE;
            case MessageConstants.FILE -> MessageConstants.FILE;
            case MessageConstants.VIDEO -> MessageConstants.VIDEO;
            default -> throw new BusinessException("不支持的消息类型");
        };

        PrivateMessages privateMessages = PrivateMessages.builder()
                .repliedToMessageId(request.getRepliedToMessageId())
                .conversationId(request.getConversationId())
                .senderId(userId)
                .type(request.getType())
                .content(content)
                .fileUrl(request.getFileUrl())
                .isRead(MessageConstants.NOT_READ).build();

        save(privateMessages);

        PrivateConversations privateConversations = PrivateConversations.builder()
                        .lastMessageId(privateMessages.getMessageId()).build();
        privateConversationsService.updateConv(privateConversations);

        // 未读计数

        // 推送
    }
}

