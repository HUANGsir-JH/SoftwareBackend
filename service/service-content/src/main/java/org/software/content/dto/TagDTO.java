// org.software.content.TagDTO封装请求参数
package org.software.content.dto;

import lombok.Data;

@Data
public class TagDTO {
    private String tagName;
    private Integer isActive;
}