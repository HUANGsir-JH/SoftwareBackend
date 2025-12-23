package org.software.model.content.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.software.model.user.UserV;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentUnreadVO {

    private Long commentId;
    private Long contentId;
    private Long userId;
    private UserV user;
    private Long toUserId;
    private UserV toUser;
    private Long parentCommentId;
    private Long rootCommentId;
    private String content;
    private String firstMedia;
    private String mediaType;
    private Integer isRead;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;

}
