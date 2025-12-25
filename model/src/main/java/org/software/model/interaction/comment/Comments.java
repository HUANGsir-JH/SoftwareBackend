package org.software.model.interaction.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评论(Comment)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("comments")
@Builder
public class Comments {
    // 评论Id
    @TableId(type = IdType.AUTO)
    private Long commentId;
    // 被评论的内容Id
    private Long contentId;
    // 评论者id
    private Long userId;
    // 父评论id
    private Long parentCommentId;
    // 根评论id
    private Long rootCommentId;
    // 回复的评论的用户id
    private Long toUserId;
    // 评论内容
    private String content;
    // 评论是否已读（0为未读，1为已读）
    private Integer isRead;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}