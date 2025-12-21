package org.software.model.social.priv;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.software.model.user.User;
import org.software.model.user.UserV;

import java.util.Date;

/**
 * 私聊会话(PrivateConversations)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("private_conversations")
@Builder
public class PrivateConversations {
    // 私聊会话id
    @TableId(type = IdType.AUTO)
    private Long conversationId;
    // 用户1id
    private Long user1Id;
    // 用户2id
    private Long user2Id;
    // 最后一次聊天时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date lastContactTime;
    // 最后一条消息id
    private Long lastMessageId;
    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;

    @TableField(exist = false)
    private Integer unreadCount;
    @TableField(exist = false)
    private UserV friend;
    @TableField(exist = false)
    private String lastMessage;
}