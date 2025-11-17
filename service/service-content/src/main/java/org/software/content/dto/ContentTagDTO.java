// org.software.content.dto.ContentTagDTO
package org.software.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContentTagDTO {
    private Integer contentId;
    private List<Integer> tagIds;
}