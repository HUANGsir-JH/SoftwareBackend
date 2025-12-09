package org.software.model.content.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ContentLikeFavoriteVO {
    private Long likeId;
    private Long contentId;
    private Long userId;
    private Integer isRead;
    private String type;
    private Date createdAt;
}
