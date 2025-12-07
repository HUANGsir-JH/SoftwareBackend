package org.software.content.dto;

import lombok.Data;
import org.software.model.content.media.ContentMedia;
import org.software.model.content.tag.Tag;

import java.util.Date;
import java.util.List;

@Data
public class ContentDetailDto {
    private Long contentId;
    private Long userId;
    private String contentType;
    private String title;
    private String description;
    private Integer isPublic;
    private String status;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Date createdAt;
    private Date updatedAt;

    private UserSimpleDto user;
    private List<ContentMedia> medias;
    private List<Tag> tags;

    private Date deletedAt;
}

