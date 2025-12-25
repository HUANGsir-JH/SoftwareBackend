package org.software.notification.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.feign.UserFeignClient;
import org.software.model.Response;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.social.UnreadCounts;
import org.software.model.social.priv.PrivateConversations;
import org.software.model.user.UserStatusV;
import org.software.notification.mapper.PrivateConversationsMapper;
import org.software.notification.mapper.UnreadCountsMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PrivateConversationsServiceImpl 白盒测试
 * 测试重点：私聊会话管理核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class PrivateConversationsServiceImplTest {

    private PrivateConversationsMapper privateConversationsMapper;
    private UserFeignClient userFeignClient;
    private UnreadCountsMapper unreadCountsMapper;
    private PrivateConversationsServiceImpl privateConversationsService;

    private PrivateConversations testConversation;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        privateConversationsMapper = mock(PrivateConversationsMapper.class);
        userFeignClient = mock(UserFeignClient.class);
        unreadCountsMapper = mock(UnreadCountsMapper.class);

        // 手动创建 PrivateConversationsServiceImpl 并注入依赖
        privateConversationsService = new PrivateConversationsServiceImpl() {
            @Override
            public boolean save(PrivateConversations entity) {
                int result = privateConversationsMapper.insert(entity);
                return result > 0;
            }
        };

        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field privateConversationsMapperField = PrivateConversationsServiceImpl.class.getDeclaredField("privateConversationsMapper");
            privateConversationsMapperField.setAccessible(true);
            privateConversationsMapperField.set(privateConversationsService, privateConversationsMapper);

            java.lang.reflect.Field userFeignClientField = PrivateConversationsServiceImpl.class.getDeclaredField("userFeignClient");
            userFeignClientField.setAccessible(true);
            userFeignClientField.set(privateConversationsService, userFeignClient);

            java.lang.reflect.Field unreadCountsMapperField = PrivateConversationsServiceImpl.class.getDeclaredField("unreadCountsMapper");
            unreadCountsMapperField.setAccessible(true);
            unreadCountsMapperField.set(privateConversationsService, unreadCountsMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }

        // 准备测试数据
        testConversation = PrivateConversations.builder()
                .conversationId(1L)
                .user1Id(100L)
                .user2Id(200L)
                .lastMessageId(1L)
                .createdAt(new Date())
                .build();
    }

    /**
     * 测试添加会话 - 正常流程（userId < friendId）
     */
    @Test
    void testAddConv_UserIdLessThanFriendId_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            Long friendId = 200L;

            // Mock 插入会话
            when(privateConversationsMapper.insert(any(PrivateConversations.class))).thenAnswer(invocation -> {
                PrivateConversations conv = invocation.getArgument(0);
                conv.setConversationId(1L);
                // 验证 user1Id 应该是较小的值
                assertEquals(100L, conv.getUser1Id());
                assertEquals(200L, conv.getUser2Id());
                return 1;
            });

            // Mock 批量插入未读计数
            when(unreadCountsMapper.batchInsert(anyList())).thenReturn(2);

            // 执行添加会话
            PrivateConversations result = privateConversationsService.addConv(friendId);

            // 验证结果
            assertNotNull(result);
            assertEquals(1L, result.getConversationId());

            // 验证调用
            verify(privateConversationsMapper, times(1)).insert(any(PrivateConversations.class));
            verify(unreadCountsMapper, times(1)).batchInsert(argThat(list -> 
                list.size() == 2 && 
                list.stream().allMatch(uc -> ((UnreadCounts)uc).getUnreadCount() == 0)
            ));
        }
    }

    /**
     * 测试添加会话 - userId > friendId
     */
    @Test
    void testAddConv_UserIdGreaterThanFriendId_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(200L);

            Long friendId = 100L;

            // Mock 插入会话
            when(privateConversationsMapper.insert(any(PrivateConversations.class))).thenAnswer(invocation -> {
                PrivateConversations conv = invocation.getArgument(0);
                conv.setConversationId(1L);
                // 验证 user1Id 应该是较小的值
                assertEquals(100L, conv.getUser1Id());
                assertEquals(200L, conv.getUser2Id());
                return 1;
            });

            // Mock 批量插入未读计数
            when(unreadCountsMapper.batchInsert(anyList())).thenReturn(2);

            // 执行添加会话
            PrivateConversations result = privateConversationsService.addConv(friendId);

            // 验证结果
            assertNotNull(result);
            assertEquals(1L, result.getConversationId());

            // 验证调用
            verify(privateConversationsMapper, times(1)).insert(any(PrivateConversations.class));
            verify(unreadCountsMapper, times(1)).batchInsert(anyList());
        }
    }

    /**
     * 测试添加会话 - userId 等于 friendId（边界情况）
     */
    @Test
    void testAddConv_UserIdEqualsFriendId() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            Long friendId = 100L;

            // Mock 插入会话
            when(privateConversationsMapper.insert(any(PrivateConversations.class))).thenAnswer(invocation -> {
                PrivateConversations conv = invocation.getArgument(0);
                conv.setConversationId(1L);
                // 当userId == friendId时，两个值应该相同
                assertEquals(100L, conv.getUser1Id());
                assertEquals(100L, conv.getUser2Id());
                return 1;
            });

            // Mock 批量插入未读计数
            when(unreadCountsMapper.batchInsert(anyList())).thenReturn(2);

            // 执行添加会话
            PrivateConversations result = privateConversationsService.addConv(friendId);

            // 验证结果
            assertNotNull(result);
        }
    }

    /**
     * 测试添加会话 - 验证未读计数初始化
     */
    @Test
    void testAddConv_UnreadCountsInitialization() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            Long friendId = 200L;

            // Mock 插入会话
            when(privateConversationsMapper.insert(any(PrivateConversations.class))).thenAnswer(invocation -> {
                PrivateConversations conv = invocation.getArgument(0);
                conv.setConversationId(1L);
                return 1;
            });

            // Mock 批量插入未读计数并验证
            when(unreadCountsMapper.batchInsert(anyList())).thenAnswer(invocation -> {
                List<UnreadCounts> ucs = invocation.getArgument(0);
                assertEquals(2, ucs.size());
                
                // 验证两个UnreadCounts的初始化
                for (UnreadCounts uc : ucs) {
                    assertEquals(1L, uc.getConversationId());
                    assertEquals(0, uc.getUnreadCount());
                    assertTrue(uc.getUserId() == 100L || uc.getUserId() == 200L);
                }
                return 2;
            });

            // 执行添加会话
            PrivateConversations result = privateConversationsService.addConv(friendId);

            // 验证结果
            assertNotNull(result);
            verify(unreadCountsMapper, times(1)).batchInsert(anyList());
        }
    }

    /**
     * 测试获取私聊列表 - 正常流程
     */
    @Test
    void testGetPrivateChatList_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            PageQuery pageQuery = new PageQuery(1, 10);

            // Mock 分页查询
            Page<PrivateConversations> page = new Page<>(1, 10);
            List<PrivateConversations> conversations = new ArrayList<>();
            conversations.add(testConversation);
            page.setRecords(conversations);
            page.setTotal(1);

            when(privateConversationsMapper.pageC(any(Page.class), eq(100L))).thenReturn(page);

            // Mock 用户信息查询
            UserStatusV userStatusV = new UserStatusV();
            userStatusV.setUserId(200L);
            userStatusV.setNickname("测试好友");
            when(userFeignClient.getUser(anyLong())).thenReturn(Response.success(userStatusV));

            // 执行查询
            PageResult result = privateConversationsService.getPrivateChatList(pageQuery);

            // 验证结果
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getPageNum());
            assertEquals(10, result.getPageSize());

            // 验证调用
            verify(privateConversationsMapper, times(1)).pageC(any(Page.class), eq(100L));
            verify(userFeignClient, times(1)).getUser(anyLong());
        }
    }

    /**
     * 测试获取私聊列表 - 空列表
     */
    @Test
    void testGetPrivateChatList_EmptyList() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            PageQuery pageQuery = new PageQuery(1, 10);

            // Mock 分页查询返回空
            Page<PrivateConversations> page = new Page<>(1, 10);
            page.setRecords(new ArrayList<>());
            page.setTotal(0);

            when(privateConversationsMapper.pageC(any(Page.class), eq(100L))).thenReturn(page);

            // 执行查询
            PageResult result = privateConversationsService.getPrivateChatList(pageQuery);

            // 验证结果
            assertNotNull(result);
            assertEquals(0, result.getTotal());

            // 验证未调用用户信息查询
            verify(userFeignClient, never()).getUser(anyLong());
        }
    }

    /**
     * 测试获取私聊列表 - 大分页
     */
    @Test
    void testGetPrivateChatList_LargePagination() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            PageQuery pageQuery = new PageQuery(5, 20);

            // Mock 分页查询
            Page<PrivateConversations> page = new Page<>(5, 20);
            List<PrivateConversations> conversations = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                PrivateConversations conv = PrivateConversations.builder()
                        .conversationId((long) i)
                        .user1Id(100L)
                        .user2Id(200L + i)
                        .build();
                conversations.add(conv);
            }
            page.setRecords(conversations);
            page.setTotal(100);

            when(privateConversationsMapper.pageC(any(Page.class), eq(100L))).thenReturn(page);

            // Mock 用户信息查询
            UserStatusV userStatusV = new UserStatusV();
            when(userFeignClient.getUser(anyLong())).thenReturn(Response.success(userStatusV));

            // 执行查询
            PageResult result = privateConversationsService.getPrivateChatList(pageQuery);

            // 验证结果
            assertNotNull(result);
            assertEquals(100, result.getTotal());
            assertEquals(5, result.getPageNum());
            assertEquals(20, result.getPageSize());

            // 验证调用了20次用户信息查询
            verify(userFeignClient, times(20)).getUser(anyLong());
        }
    }

    /**
     * 测试获取私聊列表 - 第一页
     */
    @Test
    void testGetPrivateChatList_FirstPage() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            PageQuery pageQuery = new PageQuery(1, 10);

            // Mock 分页查询
            Page<PrivateConversations> page = new Page<>(1, 10);
            page.setRecords(new ArrayList<>());
            page.setTotal(50);

            when(privateConversationsMapper.pageC(any(Page.class), eq(100L))).thenReturn(page);

            // 执行查询
            PageResult result = privateConversationsService.getPrivateChatList(pageQuery);

            // 验证结果
            assertEquals(1, result.getPageNum());
            assertEquals(50, result.getTotal());
        }
    }

    /**
     * 测试更新会话 - 正常流程
     */
    @Test
    void testUpdateConv_Success() {
        PrivateConversations conv = PrivateConversations.builder()
                .conversationId(1L)
                .lastMessageId(10L)
                .build();

        // Mock 保存操作
        when(privateConversationsMapper.insert(any(PrivateConversations.class))).thenReturn(1);

        // 执行更新
        assertDoesNotThrow(() -> privateConversationsService.updateConv(conv));

        // 验证调用
        verify(privateConversationsMapper, times(1)).insert(any(PrivateConversations.class));
    }

    /**
     * 测试更新会话 - 更新最后消息ID
     */
    @Test
    void testUpdateConv_UpdateLastMessageId() {
        PrivateConversations conv = PrivateConversations.builder()
                .conversationId(1L)
                .lastMessageId(999L)
                .build();

        // Mock 保存操作
        when(privateConversationsMapper.insert(any(PrivateConversations.class))).thenAnswer(invocation -> {
            PrivateConversations c = invocation.getArgument(0);
            assertEquals(999L, c.getLastMessageId());
            return 1;
        });

        // 执行更新
        assertDoesNotThrow(() -> privateConversationsService.updateConv(conv));

        // 验证调用
        verify(privateConversationsMapper, times(1)).insert(any(PrivateConversations.class));
    }

    /**
     * 测试更新会话 - null会话对象
     */
    @Test
    void testUpdateConv_NullConversation() {
        // Mock 保存操作
        when(privateConversationsMapper.insert(isNull())).thenReturn(0);

        // 执行更新（可能抛出异常或返回false）
        assertDoesNotThrow(() -> privateConversationsService.updateConv(null));
    }

    /**
     * 测试获取私聊列表 - 多个会话
     */
    @Test
    void testGetPrivateChatList_MultipleConversations() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            PageQuery pageQuery = new PageQuery(1, 10);

            // Mock 分页查询
            Page<PrivateConversations> page = new Page<>(1, 10);
            List<PrivateConversations> conversations = new ArrayList<>();
            
            PrivateConversations conv1 = PrivateConversations.builder()
                    .conversationId(1L)
                    .user1Id(100L)
                    .user2Id(200L)
                    .build();
            PrivateConversations conv2 = PrivateConversations.builder()
                    .conversationId(2L)
                    .user1Id(100L)
                    .user2Id(300L)
                    .build();
            PrivateConversations conv3 = PrivateConversations.builder()
                    .conversationId(3L)
                    .user1Id(100L)
                    .user2Id(400L)
                    .build();
            
            conversations.add(conv1);
            conversations.add(conv2);
            conversations.add(conv3);
            page.setRecords(conversations);
            page.setTotal(3);

            when(privateConversationsMapper.pageC(any(Page.class), eq(100L))).thenReturn(page);

            // Mock 用户信息查询
            UserStatusV userStatusV = new UserStatusV();
            when(userFeignClient.getUser(anyLong())).thenReturn(Response.success(userStatusV));

            // 执行查询
            PageResult result = privateConversationsService.getPrivateChatList(pageQuery);

            // 验证结果
            assertNotNull(result);
            assertEquals(3, result.getTotal());

            // 验证调用了3次用户信息查询
            verify(userFeignClient, times(3)).getUser(anyLong());
        }
    }
}
