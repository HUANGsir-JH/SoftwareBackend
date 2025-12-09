package org.software.model.content.dto;

import lombok.Data;

@Data
public class ContentLikeFavoriteDTO {
    private Long contentId;
    private Long userId;
    private String type;
}