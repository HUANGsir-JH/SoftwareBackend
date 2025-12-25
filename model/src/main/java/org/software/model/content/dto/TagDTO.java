// org.software.content.TagDTO封装请求参数
package org.software.model.content.dto;

import lombok.Data;

@Data
public class TagDTO {
    private Integer tagId;
    private String tagName;
    private Integer isActive;
}