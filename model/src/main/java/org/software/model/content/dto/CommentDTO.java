package org.software.model.content.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Integer contentId;
    private Integer parentCommentId;
    private Integer rootCommentId;
    private Integer toUserId;
    private String content;
    private Integer userId;

}
