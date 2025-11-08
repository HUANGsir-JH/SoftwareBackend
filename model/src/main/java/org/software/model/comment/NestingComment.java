package org.software.model.comment;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 分页评论(NestingComment)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("comment")
public class NestingComment {
    // 评论Id
    @TableId
    private Integer commentId;
    // 被评论的内容Id
    private Integer contentId;
    // 评论者id
    private Integer userId;
    // 父评论id
    private Integer parentCommentId;
    // 评论内容
    private String content;
    // 评论是否已读（0为未读，1为已读）
    private Integer isRead;
    // 嵌套评论列表
    private List<NestingComment> replies;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}