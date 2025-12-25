package org.software.notification.service.impl;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.model.constants.MessageConstants;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.social.SendMessageRequest;
import org.software.model.social.UnreadCounts;
import org.software.model.social.priv.PrivateConversations;
import org.software.model.social.priv.PrivateMessages;
import org.software.model.user.WsMsg;
import org.software.notification.mapper.PrivateMessagesMapper;
import org.software.notification.mapper.UnreadCountsMapper;
import org.software.notification.service.PrivateConversationsService;
import org.software.notification.service.PrivateMessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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
    @Autowired
    private UnreadCountsMapper unreadCountsMapper;

    @Override
    public PageResult getPrivateMessageDetail(PageQuery query, Long conversationId) throws Exception {
        if (conversationId == null) {
            throw new RuntimeException("会话ID不能为空");
        }

        Page<PrivateMessages> page = new Page<>(query.getPageNum(), query.getPageSize());
        QueryWrapper<PrivateMessages> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId)
                .orderByDesc("created_at");
        page = page(page, queryWrapper);

        // 更新已读
        List<PrivateMessages> list = page.getRecords().stream()
                .peek(item -> item.setIsRead(MessageConstants.IS_READ)).toList();
        // saveBatch(list);

        return PageResult.builder()
                .total(page.getTotal())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .records(page.getRecords())
                .build();
    }

    @Transactional
    @Override
    public void sendPrivateMessage(WsMsg request, Long userId) throws BusinessException {

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
                .conversationId(request.getConversationId())
                .user1Id(userId > request.getFriendId() ? request.getFriendId() : userId)
                .user2Id(userId > request.getFriendId() ? userId : request.getFriendId())
                .lastMessage(request.getContent())
                .lastContactTime(DateTime.now())
                .lastMessageId(privateMessages.getMessageId()).build();
        privateConversationsService.updateById(privateConversations);

        // TODO: redis优化？
        // 未读计数
        UpdateWrapper<UnreadCounts> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("unread_count = unread_count + 1")
                .eq("conversation_id", request.getConversationId())
                .eq("user_id", request.getFriendId());
        unreadCountsMapper.update(null, updateWrapper);
    }
}

