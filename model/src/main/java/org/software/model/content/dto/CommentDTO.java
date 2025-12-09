package org.software.model.content.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long contentId;
    private Long parentCommentId;
    private Long rootCommentId;
    private Long toUserId;
    private String content;
    private Long userId;

}
