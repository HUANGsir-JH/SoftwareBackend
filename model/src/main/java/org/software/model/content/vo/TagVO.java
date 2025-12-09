// org.software.content.dto.TagVO封装响应数据
package org.software.model.content.vo;

import lombok.Data;

import java.util.Date;

@Data
public class TagVO {
    private Long tagId;
    private String tagName;
    private Integer isActive;
    private Date createdAt;
    private Date updatedAt;
}