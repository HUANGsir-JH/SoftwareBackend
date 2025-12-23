package org.software.model.content.vo;

import lombok.Data;
import org.software.model.user.UserV;

import java.util.Date;

@Data
public class CommentVO {
    private Long commentId;
    private Long contentId;
    private Long userId;
    private UserV user;
    private Long parentCommentId;
    private Long rootCommentId;
    private Long toUserId;
    private UserV toUser;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
    private Integer childCount;
}
