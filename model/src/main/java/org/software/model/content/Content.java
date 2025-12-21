package org.software.model.content;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    @TableId(type = IdType.AUTO)
    private Long contentId;
    // 上传者id
    private Long userId;
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
    // 封面URL（冗余字段）
    private String coverUrl;
    // 点赞数（冗余字段）
    private Integer likeCount;
    // 收藏数（冗余字段）
    private Integer favoriteCount;
    // 评论数（冗余字段）
    private Integer commentCount;
    // 上传时间
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    // 软删除字段
    @TableLogic(value = "null", delval = "now()")
    private Date deletedAt;
    @TableField(exist = false)
    private List<String> medias;
    @TableField(exist = false)
    private List<Long> tags;
    

}

