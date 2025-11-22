package org.software.model.content.vo;

import lombok.Data;

import java.util.Date;

@Data
public class ContentLikeFavoriteVO {
    private Integer likeId;
    private Integer contentId;
    private Integer userId;
    private Integer isRead;
    private String type;
    private Date createdAt;
}
