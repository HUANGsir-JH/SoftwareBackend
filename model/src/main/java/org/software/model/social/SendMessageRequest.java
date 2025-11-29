package org.software.model.social;

import lombok.Data;

/**
 * 发送私聊消息请求
 */
@Data
public class SendMessageRequest {
    private String randomKey; // 消息唯一标识，用于防止重复消费（客户端生成）
    private Long conversationId;
    private Long friendId;
    private String type;
    private String content;
    private String fileUrl;
    private Long repliedToMessageId;
}
