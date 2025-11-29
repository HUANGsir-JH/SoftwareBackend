package org.software.notification.service.impl;

import org.software.common.util.RedisHelper;
import org.software.model.constants.WebSocketConstants;
import org.software.notification.service.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class RedisConnectionManager implements ConnectionManager {

    @Autowired
    private RedisHelper redisHelper;

    // 存储所有活跃的WebSocket会话
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    // 存储连接ID和最后活动时间
    private final ConcurrentHashMap<String, Long> lastActivityMap = new ConcurrentHashMap<>();

    @Override
    public void register(Long userId, WebSocketSession session) {
        String sessionId = session.getId();

        // 注册到Redis（用于分布式在线状态）
        redisHelper.addSet(WebSocketConstants.USER_KEY_PREFIX + userId, sessionId);
        // 注册到本地内存（用于消息推送）
        activeSessions.put(sessionId, session);
        lastActivityMap.put(sessionId, System.currentTimeMillis());
    }

    @Override
    public void unregister(Long userId, String sessionId) {
        // 从Redis移除
        redisHelper.removeSetValue(WebSocketConstants.USER_KEY_PREFIX + userId, sessionId);
        // 从本地内存移除
        activeSessions.remove(sessionId);
        lastActivityMap.remove(sessionId);
    }

    @Override
    public boolean isOnline(Long userId) {
        // 只要 Redis Set 中有元素，即表示用户在线
        return redisHelper.hasMember(WebSocketConstants.USER_KEY_PREFIX + userId) > 0;
    }

    @Override
    public void updateActivity(String sessionId) {
        lastActivityMap.put(sessionId, System.currentTimeMillis());
    }

    @Override
    public WebSocketSession getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    @Override
    public Map<String, WebSocketSession> getAllSessions() {
        return activeSessions;
    }

    @Override
    public Long getLastActivity(String sessionId) {
        return lastActivityMap.get(sessionId);
    }
}
