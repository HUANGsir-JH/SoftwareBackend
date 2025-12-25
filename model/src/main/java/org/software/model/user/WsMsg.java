package org.software.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WsMsg {
    private String randomKey; // 消息唯一标识，用于防止重复消费（客户端生成）
    private Long conversationId;
    private Long friendId;
    private String type;
    private String content;
    private String fileUrl;
    private Long repliedToMessageId;
    private Long userId;
}
