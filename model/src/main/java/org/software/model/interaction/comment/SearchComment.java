package org.software.model.interaction.comment;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.software.model.user.User;

import java.util.Date;

/**
 * 评论查询(SearchComment)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("comments")
public class SearchComment {
    // 评论Id
    @TableId
    private Integer commentId;
    // 被评论的内容Id
    private Integer contentId;
    // 评论者id
    private Integer userId;
    // 评论者用户信息
    private User user;
    // 被评论的用户id
    private Integer toUserId;
    // 被评论的用户信息
    private User toUser;
    // 父评论id
    private Integer parentCommentId;
    // 根评论id
    private Integer rootCommentId;
    // 评论内容
    private String content;
    // 子评论的数量
    private Integer childCount;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}


