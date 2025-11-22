// org.software.model.content.dto.ContentTagDTO
package org.software.model.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContentTagDTO {
    private Integer contentId;
    private List<Integer> tagIds;
}