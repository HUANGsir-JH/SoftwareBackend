package org.software.notification.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.model.constants.MessageConstants;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.social.SendMessageRequest;
import org.software.model.social.priv.PrivateConversations;
import org.software.model.social.priv.PrivateMessages;
import org.software.notification.mapper.PrivateMessagesMapper;
import org.software.notification.mapper.UnreadCountsMapper;
import org.software.notification.service.PrivateConversationsService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PrivateMessagesServiceImpl 白盒测试
 * 测试重点：私聊消息管理核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class PrivateMessagesServiceImplTest {

    private PrivateMessagesMapper privateMessagesMapper;
    private PrivateConversationsService privateConversationsService;
    private UnreadCountsMapper unreadCountsMapper;
    private PrivateMessagesServiceImpl privateMessagesService;

    private PrivateMessages testMessage;
    private SendMessageRequest sendMessageRequest;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        privateMessagesMapper = mock(PrivateMessagesMapper.class);
        privateConversationsService = mock(PrivateConversationsService.class);
        unreadCountsMapper = mock(UnreadCountsMapper.class);

        // 手动创建 PrivateMessagesServiceImpl 并注入依赖
        privateMessagesService = new PrivateMessagesServiceImpl() {
            @Override
            public Page<PrivateMessages> page(Page<PrivateMessages> page, QueryWrapper<PrivateMessages> queryWrapper) {
                return privateMessagesMapper.selectPage(page, queryWrapper);
            }

            @Override
            public boolean saveBatch(java.util.Collection<PrivateMessages> entityList) {
                entityList.forEach(entity -> privateMessagesMapper.insert(entity));
                return true;
            }

            @Override
            public boolean save(PrivateMessages entity) {
                int result = privateMessagesMapper.insert(entity);
                return result > 0;
            }
        };

        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field privateMessagesMapperField = PrivateMessagesServiceImpl.class.getDeclaredField("privateMessagesMapper");
            privateMessagesMapperField.setAccessible(true);
            privateMessagesMapperField.set(privateMessagesService, privateMessagesMapper);

            java.lang.reflect.Field privateConversationsServiceField = PrivateMessagesServiceImpl.class.getDeclaredField("privateConversationsService");
            privateConversationsServiceField.setAccessible(true);
            privateConversationsServiceField.set(privateMessagesService, privateConversationsService);

            java.lang.reflect.Field unreadCountsMapperField = PrivateMessagesServiceImpl.class.getDeclaredField("unreadCountsMapper");
            unreadCountsMapperField.setAccessible(true);
            unreadCountsMapperField.set(privateMessagesService, unreadCountsMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }

        // 准备测试数据
        testMessage = PrivateMessages.builder()
                .messageId(1L)
                .conversationId(1L)
                .senderId(100L)
                .type(MessageConstants.TEXT)
                .content("测试消息")
                .isRead(MessageConstants.NOT_READ)
                .createdTime(new Date())
                .build();

        sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setConversationId(1L);
        sendMessageRequest.setFriendId(200L);
        sendMessageRequest.setType(MessageConstants.TEXT);
        sendMessageRequest.setContent("测试消息内容");
    }

    /**
     * 测试获取私聊消息详情 - 正常流程
     */
    @Test
    void testGetPrivateMessageDetail_Success() throws Exception {
        Long conversationId = 1L;
        PageQuery pageQuery = new PageQuery(1, 10);

        // Mock 分页查询
        Page<PrivateMessages> page = new Page<>(1, 10);
        List<PrivateMessages> messages = new ArrayList<>();
        messages.add(testMessage);
        page.setRecords(messages);
        page.setTotal(1);

        when(privateMessagesMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // Mock 批量保存（更新已读状态）
        when(privateMessagesMapper.insert(any(PrivateMessages.class))).thenReturn(1);

        // 执行查询
        PageResult result = privateMessagesService.getPrivateMessageDetail(pageQuery, conversationId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());

        // 验证调用
        verify(privateMessagesMapper, times(1)).selectPage(any(Page.class), any(QueryWrapper.class));
        verify(privateMessagesMapper, times(1)).insert(any(PrivateMessages.class));
    }

    /**
     * 测试获取私聊消息详情 - 会话ID为空
     */
    @Test
    void testGetPrivateMessageDetail_ConversationIdNull() {
        PageQuery pageQuery = new PageQuery(1, 10);

        // 验证异常
        Exception exception = assertThrows(RuntimeException.class, () -> {
            privateMessagesService.getPrivateMessageDetail(pageQuery, null);
        });

        assertEquals("会话ID不能为空", exception.getMessage());
    }

    /**
     * 测试获取私聊消息详情 - 空消息列表
     */
    @Test
    void testGetPrivateMessageDetail_EmptyMessages() throws Exception {
        Long conversationId = 1L;
        PageQuery pageQuery = new PageQuery(1, 10);

        // Mock 分页查询返回空
        Page<PrivateMessages> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(privateMessagesMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // 执行查询
        PageResult result = privateMessagesService.getPrivateMessageDetail(pageQuery, conversationId);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotal());

        // 验证未调用批量保存
        verify(privateMessagesMapper, never()).insert(any(PrivateMessages.class));
    }

    /**
     * 测试获取私聊消息详情 - 多条消息
     */
    @Test
    void testGetPrivateMessageDetail_MultipleMessages() throws Exception {
        Long conversationId = 1L;
        PageQuery pageQuery = new PageQuery(1, 10);

        // Mock 分页查询
        Page<PrivateMessages> page = new Page<>(1, 10);
        List<PrivateMessages> messages = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PrivateMessages msg = PrivateMessages.builder()
                    .messageId((long) i)
                    .conversationId(1L)
                    .senderId(100L)
                    .content("消息" + i)
                    .isRead(MessageConstants.NOT_READ)
                    .build();
            messages.add(msg);
        }
        page.setRecords(messages);
        page.setTotal(5);

        when(privateMessagesMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // Mock 批量保存
        when(privateMessagesMapper.insert(any(PrivateMessages.class))).thenReturn(1);

        // 执行查询
        PageResult result = privateMessagesService.getPrivateMessageDetail(pageQuery, conversationId);

        // 验证结果
        assertEquals(5, result.getTotal());

        // 验证调用了5次插入（批量更新已读状态）
        verify(privateMessagesMapper, times(5)).insert(any(PrivateMessages.class));
    }

    /**
     * 测试发送私聊消息 - 文本消息
     */
    @Test
    void testSendPrivateMessage_TextMessage_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            sendMessageRequest.setType(MessageConstants.TEXT);
            sendMessageRequest.setContent("测试文本消息");

            // Mock 插入消息
            when(privateMessagesMapper.insert(any(PrivateMessages.class))).thenAnswer(invocation -> {
                PrivateMessages msg = invocation.getArgument(0);
                msg.setMessageId(1L);
                assertEquals("测试文本消息", msg.getContent());
                assertEquals(MessageConstants.TEXT, msg.getType());
                return 1;
            });

            // Mock 更新会话
            doNothing().when(privateConversationsService).updateConv(any(PrivateConversations.class));

            // Mock 更新未读计数
            when(unreadCountsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

            // 执行发送消息
            assertDoesNotThrow(() -> privateMessagesService.sendPrivateMessage(sendMessageRequest));

            // 验证调用
            verify(privateMessagesMapper, times(1)).insert(any(PrivateMessages.class));
            verify(privateConversationsService, times(1)).updateConv(any(PrivateConversations.class));
            verify(unreadCountsMapper, times(1)).update(isNull(), any(UpdateWrapper.class));
        }
    }

    /**
     * 测试发送私聊消息 - 图片消息
     */
    @Test
    void testSendPrivateMessage_ImageMessage_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            sendMessageRequest.setType(MessageConstants.IMAGE);
            sendMessageRequest.setFileUrl("http://example.com/image.jpg");

            // Mock 插入消息
            when(privateMessagesMapper.insert(any(PrivateMessages.class))).thenAnswer(invocation -> {
                PrivateMessages msg = invocation.getArgument(0);
                msg.setMessageId(1L);
                assertEquals(MessageConstants.IMAGE, msg.getContent());
                assertEquals(MessageConstants.IMAGE, msg.getType());
                assertEquals("http://example.com/image.jpg", msg.getFileUrl());
                return 1;
            });

            // Mock 更新会话
            doNothing().when(privateConversationsService).updateConv(any(PrivateConversations.class));

            // Mock 更新未读计数
            when(unreadCountsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

            // 执行发送消息
            assertDoesNotThrow(() -> privateMessagesService.sendPrivateMessage(sendMessageRequest));

            // 验证调用
            verify(privateMessagesMapper, times(1)).insert(any(PrivateMessages.class));
        }
    }

    /**
     * 测试发送私聊消息 - 文件消息
     */
    @Test
    void testSendPrivateMessage_FileMessage_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            sendMessageRequest.setType(MessageConstants.FILE);
            sendMessageRequest.setFileUrl("http://example.com/file.pdf");

            // Mock 插入消息
            when(privateMessagesMapper.insert(any(PrivateMessages.class))).thenAnswer(invocation -> {
                PrivateMessages msg = invocation.getArgument(0);
                msg.setMessageId(1L);
                assertEquals(MessageConstants.FILE, msg.getContent());
                assertEquals(MessageConstants.FILE, msg.getType());
                return 1;
            });

            // Mock 更新会话
            doNothing().when(privateConversationsService).updateConv(any(PrivateConversations.class));

            // Mock 更新未读计数
            when(unreadCountsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

            // 执行发送消息
            assertDoesNotThrow(() -> privateMessagesService.sendPrivateMessage(sendMessageRequest));

            // 验证调用
            verify(privateMessagesMapper, times(1)).insert(any(PrivateMessages.class));
        }
    }

    /**
     * 测试发送私聊消息 - 视频消息
     */
    @Test
    void testSendPrivateMessage_VideoMessage_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            sendMessageRequest.setType(MessageConstants.VIDEO);
            sendMessageRequest.setFileUrl("http://example.com/video.mp4");

            // Mock 插入消息
            when(privateMessagesMapper.insert(any(PrivateMessages.class))).thenAnswer(invocation -> {
                PrivateMessages msg = invocation.getArgument(0);
                msg.setMessageId(1L);
                assertEquals(MessageConstants.VIDEO, msg.getContent());
                assertEquals(MessageConstants.VIDEO, msg.getType());
                return 1;
            });

            // Mock 更新会话
            doNothing().when(privateConversationsService).updateConv(any(PrivateConversations.class));

            // Mock 更新未读计数
            when(unreadCountsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

            // 执行发送消息
            assertDoesNotThrow(() -> privateMessagesService.sendPrivateMessage(sendMessageRequest));

            // 验证调用
            verify(privateMessagesMapper, times(1)).insert(any(PrivateMessages.class));
        }
    }

    /**
     * 测试发送私聊消息 - 不支持的消息类型
     */
    @Test
    void testSendPrivateMessage_UnsupportedMessageType() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            sendMessageRequest.setType("UNSUPPORTED_TYPE");

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                privateMessagesService.sendPrivateMessage(sendMessageRequest);
            });

            assertEquals("不支持的消息类型", exception.getMessage());

            // 验证未插入消息
            verify(privateMessagesMapper, never()).insert(any(PrivateMessages.class));
        }
    }

    /**
     * 测试发送私聊消息 - 带回复消息ID
     */
    @Test
    void testSendPrivateMessage_WithRepliedToMessageId() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            sendMessageRequest.setType(MessageConstants.TEXT);
            sendMessageRequest.setContent("回复消息");
            sendMessageRequest.setRepliedToMessageId(999L);

            // Mock 插入消息
            when(privateMessagesMapper.insert(any(PrivateMessages.class))).thenAnswer(invocation -> {
                PrivateMessages msg = invocation.getArgument(0);
                msg.setMessageId(1L);
                assertEquals(999L, msg.getRepliedToMessageId());
                return 1;
            });

            // Mock 更新会话
            doNothing().when(privateConversationsService).updateConv(any(PrivateConversations.class));

            // Mock 更新未读计数
            when(unreadCountsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

            // 执行发送消息
            assertDoesNotThrow(() -> privateMessagesService.sendPrivateMessage(sendMessageRequest));

            // 验证调用
            verify(privateMessagesMapper, times(1)).insert(any(PrivateMessages.class));
        }
    }

    /**
     * 测试发送私聊消息 - 验证未读计数更新
     */
    @Test
    void testSendPrivateMessage_VerifyUnreadCountUpdate() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            sendMessageRequest.setType(MessageConstants.TEXT);
            sendMessageRequest.setContent("测试消息");
            sendMessageRequest.setConversationId(1L);
            sendMessageRequest.setFriendId(200L);

            // Mock 插入消息
            when(privateMessagesMapper.insert(any(PrivateMessages.class))).thenAnswer(invocation -> {
                PrivateMessages msg = invocation.getArgument(0);
                msg.setMessageId(1L);
                return 1;
            });

            // Mock 更新会话
            doNothing().when(privateConversationsService).updateConv(any(PrivateConversations.class));

            // Mock 更新未读计数并验证参数
            when(unreadCountsMapper.update(isNull(), any(UpdateWrapper.class))).thenAnswer(invocation -> {
                UpdateWrapper<?> wrapper = invocation.getArgument(1);
                // 这里可以验证UpdateWrapper的条件
                return 1;
            });

            // 执行发送消息
            assertDoesNotThrow(() -> privateMessagesService.sendPrivateMessage(sendMessageRequest));

            // 验证调用
            verify(unreadCountsMapper, times(1)).update(isNull(), any(UpdateWrapper.class));
        }
    }

    /**
     * 测试发送私聊消息 - 验证会话更新
     */
    @Test
    void testSendPrivateMessage_VerifyConversationUpdate() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            sendMessageRequest.setType(MessageConstants.TEXT);
            sendMessageRequest.setContent("测试消息");

            // Mock 插入消息
            when(privateMessagesMapper.insert(any(PrivateMessages.class))).thenAnswer(invocation -> {
                PrivateMessages msg = invocation.getArgument(0);
                msg.setMessageId(1L);
                return 1;
            });

            // Mock 更新会话并验证
            doAnswer(invocation -> {
                PrivateConversations conv = invocation.getArgument(0);
                assertEquals(1L, conv.getLastMessageId());
                return null;
            }).when(privateConversationsService).updateConv(any(PrivateConversations.class));

            // Mock 更新未读计数
            when(unreadCountsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

            // 执行发送消息
            assertDoesNotThrow(() -> privateMessagesService.sendPrivateMessage(sendMessageRequest));

            // 验证调用
            verify(privateConversationsService, times(1)).updateConv(any(PrivateConversations.class));
        }
    }

    /**
     * 测试获取私聊消息详情 - 大分页
     */
    @Test
    void testGetPrivateMessageDetail_LargePagination() throws Exception {
        Long conversationId = 1L;
        PageQuery pageQuery = new PageQuery(5, 50);

        // Mock 分页查询
        Page<PrivateMessages> page = new Page<>(5, 50);
        page.setRecords(new ArrayList<>());
        page.setTotal(200);

        when(privateMessagesMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // 执行查询
        PageResult result = privateMessagesService.getPrivateMessageDetail(pageQuery, conversationId);

        // 验证结果
        assertNotNull(result);
        assertEquals(200, result.getTotal());
        assertEquals(5, result.getPageNum());
        assertEquals(50, result.getPageSize());
    }
}
