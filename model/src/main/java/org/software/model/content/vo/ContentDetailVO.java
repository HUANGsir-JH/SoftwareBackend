package org.software.model.content.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.software.model.content.Tag;
import org.software.model.media.ContentMedia;
import org.software.model.media.Media;
import org.software.model.user.UserV;

import java.util.Date;
import java.util.List;

@Data
public class ContentDetailVO {
    // 内容id
    private Long contentId;
    // 上传者id
    private Long userId;
    private UserV user;
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
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;

    private List<ContentMedia> medias;
    private List<Tag> tags;
}
