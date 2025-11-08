package org.software.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 私聊会话(PrivateConversations)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("private_conversations")
public class PrivateConversations {
    // 私聊会话id
    @TableId
    private Integer conversationId;
    // 用户1id
    private Integer user1Id;
    // 用户2id
    private Integer user2Id;
    // 最后一次聊天时间
    private Date lastContactTime;
    // 最后一条消息id
    private Integer lastMessageId;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}