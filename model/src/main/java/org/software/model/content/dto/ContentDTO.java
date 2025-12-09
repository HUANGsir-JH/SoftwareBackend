package org.software.model.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentDTO {
    private Long contentId;
    // 内容类型
    private String contentType;
    // 标题
    private String title;
    // 正文
    private String description;
    // 是否公开（0为私有，1为公开）
    private Integer isPublic;
    // 帖子的状态
    private String status;

    private List<String> medias;
    private List<Long> tags;
}