package org.software.content.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CommentVO {
    private Integer commentId;
    private Integer contentId;
    private Integer userId;
    private Integer parentCommentId;
    private Integer rootCommentId;
    private Integer toUserId;
    private String content;
    private Integer isRead;
    private Date createdAt;
    private List<CommentVO> children;
}
