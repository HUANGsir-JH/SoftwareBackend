package org.software.model.interaction;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 内容点赞收藏记录(ContentLikesFavorites)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("content_likes_favorites")
public class ContentLikesFavorites {
    // 点赞记录id
    @TableId
    private Integer id;
    // 被点赞的内容id
    private Integer contentId;
    // 点赞用户id
    private Integer userId;
    // 用户是否已读（0为未读，1为已读）
    private Integer isRead;
    // 类型（like：点赞记录）
    private String type;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}