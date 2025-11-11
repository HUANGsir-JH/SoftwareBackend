package org.software.model.content.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.software.model.content.media.ContentMedia;
import org.software.model.user.User;

import java.util.Date;

/**
 * 帖子简版(PostSimple)实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostE
{
    // 内容id
    private Integer contentId;
    // 上传者id
    private Integer userId;
    // 上传者用户信息
    private User user;
    // 内容类型
    private String contentType;
    // 首条媒体信息
    private ContentMedia firstMedia;
    // 标题
    private String title;
    // 点赞数
    private Integer likeCount;
    // 上传时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}
