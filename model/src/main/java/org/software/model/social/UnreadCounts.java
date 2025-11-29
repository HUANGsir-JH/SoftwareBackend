package org.software.model.social;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 会话未读消息数记录表(UnreadCounts)表实体类
 *
 * @author Ra1nbot
 * @since 2025-11-29 21:09:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("unread_counts")
@Builder
public class UnreadCounts {
//计数记录id
    @TableId(type = IdType.ASSIGN_ID)
    private Long countId;
//当前记录所属用户
    private Long userId;
//被计数的会话id（私聊会话）
    private Long conversationId;
//当前会话未读消息数
    private Integer unreadCount;
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    private Date deletedAt;



}

