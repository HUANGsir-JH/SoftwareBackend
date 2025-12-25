package org.software.notification.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.common.util.RedisHelper;
import org.software.model.constants.WebSocketConstants;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisConnectionManager 白盒测试
 * 测试重点：WebSocket连接管理核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class RedisConnectionManagerTest {

    private RedisHelper redisHelper;
    private RedisConnectionManager connectionManager;
    private WebSocketSession mockSession;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        redisHelper = mock(RedisHelper.class);
        mockSession = mock(WebSocketSession.class);

        // 手动创建 RedisConnectionManager 并注入依赖
        connectionManager = new RedisConnectionManager();

        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field redisHelperField = RedisConnectionManager.class.getDeclaredField("redisHelper");
            redisHelperField.setAccessible(true);
            redisHelperField.set(connectionManager, redisHelper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    /**
     * 测试注册连接 - 正常流程
     */
    @Test
    void testRegister_Success() {
        Long userId = 100L;
        String sessionId = "session-123";

        // Mock session
        when(mockSession.getId()).thenReturn(sessionId);

        // Mock Redis操作
        doNothing().when(redisHelper).addSet(eq(WebSocketConstants.USER_KEY_PREFIX + userId), eq(sessionId));

        // 执行注册
        assertDoesNotThrow(() -> connectionManager.register(userId, mockSession));

        // 验证调用
        verify(redisHelper, times(1)).addSet(eq(WebSocketConstants.USER_KEY_PREFIX + userId), eq(sessionId));
        verify(mockSession, times(1)).getId();

        // 验证本地存储
        WebSocketSession retrievedSession = connectionManager.getSession(sessionId);
        assertNotNull(retrievedSession);
        assertEquals(mockSession, retrievedSession);

        // 验证最后活动时间
        Long lastActivity = connectionManager.getLastActivity(sessionId);
        assertNotNull(lastActivity);
        assertTrue(lastActivity > 0);
    }

    /**
     * 测试注册连接 - 多个用户
     */
    @Test
    void testRegister_MultipleUsers() {
        Long userId1 = 100L;
        Long userId2 = 200L;
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";

        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);

        when(session1.getId()).thenReturn(sessionId1);
        when(session2.getId()).thenReturn(sessionId2);

        // 执行注册
        connectionManager.register(userId1, session1);
        connectionManager.register(userId2, session2);

        // 验证调用
        verify(redisHelper, times(1)).addSet(eq(WebSocketConstants.USER_KEY_PREFIX + userId1), eq(sessionId1));
        verify(redisHelper, times(1)).addSet(eq(WebSocketConstants.USER_KEY_PREFIX + userId2), eq(sessionId2));

        // 验证本地存储
        assertEquals(session1, connectionManager.getSession(sessionId1));
        assertEquals(session2, connectionManager.getSession(sessionId2));
    }

    /**
     * 测试注册连接 - 同一用户多个会话
     */
    @Test
    void testRegister_SameUserMultipleSessions() {
        Long userId = 100L;
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";

        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);

        when(session1.getId()).thenReturn(sessionId1);
        when(session2.getId()).thenReturn(sessionId2);

        // 执行注册
        connectionManager.register(userId, session1);
        connectionManager.register(userId, session2);

        // 验证调用（同一用户可以有多个会话）
        verify(redisHelper, times(1)).addSet(eq(WebSocketConstants.USER_KEY_PREFIX + userId), eq(sessionId1));
        verify(redisHelper, times(1)).addSet(eq(WebSocketConstants.USER_KEY_PREFIX + userId), eq(sessionId2));

        // 验证本地存储
        assertEquals(session1, connectionManager.getSession(sessionId1));
        assertEquals(session2, connectionManager.getSession(sessionId2));
    }

    /**
     * 测试注销连接 - 正常流程
     */
    @Test
    void testUnregister_Success() {
        Long userId = 100L;
        String sessionId = "session-123";

        // 先注册
        when(mockSession.getId()).thenReturn(sessionId);
        connectionManager.register(userId, mockSession);

        // Mock Redis操作
        doNothing().when(redisHelper).removeSetValue(eq(WebSocketConstants.USER_KEY_PREFIX + userId), eq(sessionId));

        // 执行注销
        assertDoesNotThrow(() -> connectionManager.unregister(userId, sessionId));

        // 验证调用
        verify(redisHelper, times(1)).removeSetValue(eq(WebSocketConstants.USER_KEY_PREFIX + userId), eq(sessionId));

        // 验证本地存储已清除
        WebSocketSession retrievedSession = connectionManager.getSession(sessionId);
        assertNull(retrievedSession);

        // 验证最后活动时间已清除
        Long lastActivity = connectionManager.getLastActivity(sessionId);
        assertNull(lastActivity);
    }

    /**
     * 测试注销连接 - 未注册的会话
     */
    @Test
    void testUnregister_UnregisteredSession() {
        Long userId = 100L;
        String sessionId = "unregistered-session";

        // Mock Redis操作
        doNothing().when(redisHelper).removeSetValue(eq(WebSocketConstants.USER_KEY_PREFIX + userId), eq(sessionId));

        // 执行注销（不应抛出异常）
        assertDoesNotThrow(() -> connectionManager.unregister(userId, sessionId));

        // 验证调用
        verify(redisHelper, times(1)).removeSetValue(eq(WebSocketConstants.USER_KEY_PREFIX + userId), eq(sessionId));
    }

    /**
     * 测试检查在线状态 - 用户在线
     */
    @Test
    void testIsOnline_UserOnline() {
        Long userId = 100L;

        // Mock Redis检查（返回大于0表示在线）
        when(redisHelper.hasMember(eq(WebSocketConstants.USER_KEY_PREFIX + userId))).thenReturn(1L);

        // 执行检查
        boolean isOnline = connectionManager.isOnline(userId);

        // 验证结果
        assertTrue(isOnline);

        // 验证调用
        verify(redisHelper, times(1)).hasMember(eq(WebSocketConstants.USER_KEY_PREFIX + userId));
    }

    /**
     * 测试检查在线状态 - 用户离线
     */
    @Test
    void testIsOnline_UserOffline() {
        Long userId = 100L;

        // Mock Redis检查（返回0表示离线）
        when(redisHelper.hasMember(eq(WebSocketConstants.USER_KEY_PREFIX + userId))).thenReturn(0L);

        // 执行检查
        boolean isOnline = connectionManager.isOnline(userId);

        // 验证结果
        assertFalse(isOnline);

        // 验证调用
        verify(redisHelper, times(1)).hasMember(eq(WebSocketConstants.USER_KEY_PREFIX + userId));
    }

    /**
     * 测试检查在线状态 - 用户有多个会话
     */
    @Test
    void testIsOnline_UserWithMultipleSessions() {
        Long userId = 100L;

        // Mock Redis检查（返回大于1表示有多个会话）
        when(redisHelper.hasMember(eq(WebSocketConstants.USER_KEY_PREFIX + userId))).thenReturn(2L);

        // 执行检查
        boolean isOnline = connectionManager.isOnline(userId);

        // 验证结果
        assertTrue(isOnline);
    }

    /**
     * 测试更新活动时间 - 正常流程
     */
    @Test
    void testUpdateActivity_Success() {
        String sessionId = "session-123";

        // 先注册
        when(mockSession.getId()).thenReturn(sessionId);
        connectionManager.register(100L, mockSession);

        // 获取初始活动时间
        Long initialActivity = connectionManager.getLastActivity(sessionId);
        assertNotNull(initialActivity);

        // 等待一小段时间
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 更新活动时间
        connectionManager.updateActivity(sessionId);

        // 获取更新后的活动时间
        Long updatedActivity = connectionManager.getLastActivity(sessionId);
        assertNotNull(updatedActivity);

        // 验证活动时间已更新（应该比初始时间晚）
        assertTrue(updatedActivity >= initialActivity);
    }

    /**
     * 测试更新活动时间 - 未注册的会话
     */
    @Test
    void testUpdateActivity_UnregisteredSession() {
        String sessionId = "unregistered-session";

        // 执行更新（不应抛出异常）
        assertDoesNotThrow(() -> connectionManager.updateActivity(sessionId));

        // 验证活动时间存在（即使会话未注册也会创建记录）
        Long lastActivity = connectionManager.getLastActivity(sessionId);
        assertNotNull(lastActivity);
    }

    /**
     * 测试获取会话 - 存在的会话
     */
    @Test
    void testGetSession_ExistingSession() {
        Long userId = 100L;
        String sessionId = "session-123";

        // 注册会话
        when(mockSession.getId()).thenReturn(sessionId);
        connectionManager.register(userId, mockSession);

        // 执行获取
        WebSocketSession session = connectionManager.getSession(sessionId);

        // 验证结果
        assertNotNull(session);
        assertEquals(mockSession, session);
    }

    /**
     * 测试获取会话 - 不存在的会话
     */
    @Test
    void testGetSession_NonExistingSession() {
        String sessionId = "non-existing-session";

        // 执行获取
        WebSocketSession session = connectionManager.getSession(sessionId);

        // 验证结果
        assertNull(session);
    }

    /**
     * 测试获取所有会话 - 正常流程
     */
    @Test
    void testGetAllSessions_Success() {
        Long userId1 = 100L;
        Long userId2 = 200L;
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";

        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);

        when(session1.getId()).thenReturn(sessionId1);
        when(session2.getId()).thenReturn(sessionId2);

        // 注册多个会话
        connectionManager.register(userId1, session1);
        connectionManager.register(userId2, session2);

        // 执行获取所有会话
        Map<String, WebSocketSession> allSessions = connectionManager.getAllSessions();

        // 验证结果
        assertNotNull(allSessions);
        assertEquals(2, allSessions.size());
        assertTrue(allSessions.containsKey(sessionId1));
        assertTrue(allSessions.containsKey(sessionId2));
        assertEquals(session1, allSessions.get(sessionId1));
        assertEquals(session2, allSessions.get(sessionId2));
    }

    /**
     * 测试获取所有会话 - 空列表
     */
    @Test
    void testGetAllSessions_EmptyList() {
        // 执行获取所有会话
        Map<String, WebSocketSession> allSessions = connectionManager.getAllSessions();

        // 验证结果
        assertNotNull(allSessions);
        assertEquals(0, allSessions.size());
    }

    /**
     * 测试获取最后活动时间 - 存在的会话
     */
    @Test
    void testGetLastActivity_ExistingSession() {
        Long userId = 100L;
        String sessionId = "session-123";

        // 注册会话
        when(mockSession.getId()).thenReturn(sessionId);
        connectionManager.register(userId, mockSession);

        // 执行获取
        Long lastActivity = connectionManager.getLastActivity(sessionId);

        // 验证结果
        assertNotNull(lastActivity);
        assertTrue(lastActivity > 0);
    }

    /**
     * 测试获取最后活动时间 - 不存在的会话
     */
    @Test
    void testGetLastActivity_NonExistingSession() {
        String sessionId = "non-existing-session";

        // 执行获取
        Long lastActivity = connectionManager.getLastActivity(sessionId);

        // 验证结果
        assertNull(lastActivity);
    }

    /**
     * 测试完整流程 - 注册、更新、注销
     */
    @Test
    void testCompleteFlow_RegisterUpdateUnregister() {
        Long userId = 100L;
        String sessionId = "session-123";

        when(mockSession.getId()).thenReturn(sessionId);

        // 1. 注册
        connectionManager.register(userId, mockSession);
        assertNotNull(connectionManager.getSession(sessionId));
        assertNotNull(connectionManager.getLastActivity(sessionId));

        // 2. 更新活动时间
        connectionManager.updateActivity(sessionId);
        assertNotNull(connectionManager.getLastActivity(sessionId));

        // 3. 注销
        connectionManager.unregister(userId, sessionId);
        assertNull(connectionManager.getSession(sessionId));
        assertNull(connectionManager.getLastActivity(sessionId));
    }

    /**
     * 测试并发注册 - 多个会话同时注册
     */
    @Test
    void testConcurrentRegister() {
        int sessionCount = 10;

        for (int i = 0; i < sessionCount; i++) {
            Long userId = (long) i;
            String sessionId = "session-" + i;
            WebSocketSession session = mock(WebSocketSession.class);
            when(session.getId()).thenReturn(sessionId);

            connectionManager.register(userId, session);
        }

        // 验证所有会话都已注册
        Map<String, WebSocketSession> allSessions = connectionManager.getAllSessions();
        assertEquals(sessionCount, allSessions.size());
    }
}
