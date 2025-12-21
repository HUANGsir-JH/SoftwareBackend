package org.software.model.social.priv;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("private_messages")
@Builder
public class PrivateMessages {
    // 消息id
    @TableId(type = IdType.AUTO)
    private Long messageId;
    // 所属私人会话id
    private Long conversationId;
    // 发送者id
    private Long senderId;
    // 消息类型
    private String type;
    // 文本消息内容
    private String content;
    // 非文本文件路径
    private String fileUrl;
    // 回复的消息id
    private Long repliedToMessageId;
    // 消息是否已读（0为未读，1为已读）
    private Integer isRead;
    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}