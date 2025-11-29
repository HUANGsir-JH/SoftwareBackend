package org.software.notification.service;

import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// websocket-service/src/main/java/.../ConnectionManager.java
public interface ConnectionManager {
    final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    final ConcurrentHashMap<String, Long> lastActivityMap = new ConcurrentHashMap<>();

    void register(Long userId, WebSocketSession session); // 注册连接（同时注册到Redis和内存）
    void unregister(Long userId, String sessionId); // 移除连接（同时从Redis和内存移除）
    boolean isOnline(Long userId); // 判断用户是否在线
    void updateActivity(String sessionId); // 更新活动时间
    WebSocketSession getSession(String sessionId); // 获取指定会话
    Map<String, WebSocketSession> getAllSessions(); // 获取所有活跃会话
    Long getLastActivity(String sessionId); // 获取最后活动时间
}

