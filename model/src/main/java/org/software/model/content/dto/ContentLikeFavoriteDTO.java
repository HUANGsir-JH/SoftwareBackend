package org.software.model.content.dto;

import lombok.Data;

@Data
public class ContentLikeFavoriteDTO {
    private Integer contentId;
    private Integer userId;
    private String type;
}