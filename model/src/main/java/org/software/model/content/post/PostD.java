package org.software.model.content.post;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.software.model.media.ContentMedia;
import org.software.model.content.tag.Tag;

import java.util.Date;
import java.util.List;

/**
 * 帖子详情(PostDetail)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostD {
    // 内容id
    @TableId
    private Integer contentId;
    // 上传者id
    private Integer userId;
    // 内容类型
    private String contentType;
    // 媒体文件列表
    private List<ContentMedia> medias;
    // 标签列表
    private List<Tag> tags;
    // 标题
    private String title;
    // 正文
    private String description;
    // 是否公开（0为私有，1为公开）
    private Integer isPublic;
    // 状态
    private String status;
    // 上传时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}