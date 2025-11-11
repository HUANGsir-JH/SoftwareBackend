package org.software.model.content;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 内容(Content)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("content")
public class Content {
    // 内容id
    @TableId(type = IdType.ASSIGN_ID)
    private Long contentId;
    // 上传者id
    private Integer userId;
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
    // 上传时间
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}