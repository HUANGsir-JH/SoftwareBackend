package org.software.model.content.vo;

import lombok.Data;
import org.software.model.user.UserV;

import java.util.Date;
import java.util.List;

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
    private Integer isRead;
    private Date createdAt;
    private List<CommentVO> children;
}
