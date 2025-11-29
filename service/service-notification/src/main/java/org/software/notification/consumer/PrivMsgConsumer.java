package org.software.notification.consumer;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.software.common.util.RedisHelper;
import org.software.model.constants.MessageConstants;
import org.software.model.exception.BusinessException;
import org.software.model.social.SendMessageRequest;
import org.software.notification.service.ConnectionManager;
import org.software.notification.service.PrivateMessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 私聊消息消费者
 * 1. 接收用户发来的消息
 * 2. 调用sendPrivateMessage接口保存消息
 * 3. 如果接收者在线，则推送消息
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${rocketmq.topic}",
        consumerGroup = "service-notification-consumer",
        selectorExpression = "msg"  // 只消费tag为msg的消息
)
public class PrivMsgConsumer implements RocketMQListener<String> {

    @Autowired
    private PrivateMessagesService privateMessagesService;
    @Autowired
    private ConnectionManager connectionManager;
    @Autowired
    private RedisHelper redisHelper;

    @Override
    public void onMessage(String message) {
        log.info("收到私聊消息: {}", message);
        
        try {
            // 1. 解析消息
            SendMessageRequest request = JSONUtil.toBean(message, SendMessageRequest.class);
            
            // 2. 消息去重检查
            if (request.getRandomKey() == null || request.getRandomKey().isEmpty()) {
                log.warn("消息缺少randomKey，无法去重");
                return;
            }
            
            String dedupKey = MessageConstants.MESSAGE_DEDUP_KEY + request.getRandomKey();
            boolean isFirstTime = redisHelper.setIfAbsent(dedupKey, "1", 
                    MessageConstants.MESSAGE_DEDUP_TIMEOUT, TimeUnit.MINUTES);
            
            if (!isFirstTime) {
                log.warn("检测到重复消息，已忽略，randomKey: {}", request.getRandomKey());
                return;
            }
            
            // 3. 调用service保存消息到数据库
            privateMessagesService.sendPrivateMessage(request);
            log.info("消息已保存: conversationId={}, senderId={}", request.getConversationId(), request.getFriendId());
            
            // 4. 判断接收者是否在线
            Long receiverId = request.getFriendId();
            if (connectionManager.isOnline(receiverId)) {
                // 5. 如果在线，推送消息
                sendMessageToUser(receiverId, message);
                log.info("消息已推送给在线用户: userId={}", receiverId);
            } else {
                log.info("用户 {} 不在线，消息将在其上线后拉取", receiverId);
            }
            
        } catch (BusinessException e) {
            log.error("处理私聊消息业务异常: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("处理私聊消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 向指定用户推送消息
     */
    private void sendMessageToUser(Long userId, String message) {
        // 遍历所有活跃会话，找到该用户的所有连接
        for (Map.Entry<String, WebSocketSession> entry : connectionManager.getAllSessions().entrySet()) {
            WebSocketSession session = entry.getValue();
            Long sessionUserId = (Long) session.getAttributes().get("userId");
            
            if (sessionUserId != null && sessionUserId.equals(userId)) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(message));
                        log.debug("消息已发送到sessionId: {}", entry.getKey());
                    }
                } catch (IOException e) {
                    log.error("向用户 {} 发送消息失败: {}", userId, e.getMessage());
                }
            }
        }
    }
}
