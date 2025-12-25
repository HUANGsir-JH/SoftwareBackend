package org.software.notification.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.software.common.util.MQHelper;
import org.software.model.constants.WebSocketConstants;
import org.software.model.user.WsMsg;
import org.software.notification.service.ConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;


@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    public static final String tag = "msg";

    @Autowired
    private ConnectionManager connectionManager; // 注入连接管理器
    @Autowired
    private MQHelper mqHelper;

    // 1. 连接建立后
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从握手拦截器存入的attributes中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            log.error("连接建立失败：未找到用户ID");
            session.close();
            return;
        }
        
        // 注册连接（同时注册到Redis和本地内存）
        connectionManager.register(userId, session);
        
        log.info("用户 {} 建立WebSocket连接，sessionId: {}", userId, session.getId());
    }

    // 2. 收到消息后 (用于处理客户端Pong或应用层心跳)
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 接收到任何消息（包括客户端发来的 Pong 消息/应用层心跳）
        connectionManager.updateActivity(session.getId());
        
        // 可以在这里处理客户端发来的消息（如聊天消息、Pong响应）
        Long userId = (Long) session.getAttributes().get("userId");
        if (message.getPayload().equals("pong")) {
            // 这是一个应用层心跳响应
            connectionManager.updateActivity(session.getId());
            log.debug("收到用户 {} 的Pong响应", userId);
        } else {
            connectionManager.updateActivity(session.getId());
            WsMsg msg = JSONUtil.toBean(message.getPayload(), WsMsg.class);
            msg.setUserId(userId);
            SendResult sendResult = mqHelper.syncSend(tag, msg);
            log.info("收到用户 {} 的消息: {}", userId, sendResult);
        }
    }

    // 3. 连接关闭后
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            // 从Redis和内存中移除连接
            connectionManager.unregister(userId, session.getId());
            log.info("用户 {} 断开WebSocket连接，原因: {}", userId, status);
        } else {
            log.warn("连接关闭但未找到用户ID，sessionId: {}", session.getId());
        }
    }

    // 4. 服务端主动心跳和超时检测 (定时任务)
    @Scheduled(fixedRate = 10000) // 每10秒执行一次心跳检测
    public void checkAndSendHeartbeats() {
        long now = System.currentTimeMillis();
        // 遍历所有活跃连接
        for (Map.Entry<String, WebSocketSession> entry : connectionManager.getAllSessions().entrySet()) {
            String sessionId = entry.getKey();
            WebSocketSession session = entry.getValue();
            
            Long lastActivity = connectionManager.getLastActivity(sessionId);
            if (lastActivity == null) {
                continue;
            }
            
            // 检查连接是否超时
            if (now - lastActivity > WebSocketConstants.HEARTBEAT_TIMEOUT) {
                // 超时：关闭连接，触发 afterConnectionClosed
                Long userId = (Long) session.getAttributes().get("userId");
                log.warn("用户 {} 心跳超时，关闭连接", userId);
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException e) {
                    log.error("关闭超时连接失败", e);
                }
            } else if (now - lastActivity > WebSocketConstants.HEARTBEAT_TIMEOUT / 3) {
                // 主动发送 Ping 请求客户端回应
                try {
                    session.sendMessage(new TextMessage("ping"));
                } catch (IOException e) {
                    log.error("发送心跳Ping失败", e);
                }
            }
        }
    }
}