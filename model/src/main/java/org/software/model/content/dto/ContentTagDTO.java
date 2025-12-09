// org.software.model.content.dto.ContentTagDTO
package org.software.model.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContentTagDTO {
    private Long contentId;
    private List<Long> tagIds;
}