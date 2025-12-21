package org.software.model.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 内容点赞收藏记录(ContentLikeFavorite)表实体类
 *
 * @author Ra1nbot
 * @since 2025-11-08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//@TableName("content_like_favorite")
public class ContentLikeFavorite {
    // 点赞记录id
    @TableId(type = IdType.AUTO)
    private Long likeId;
    // 被点赞的内容id
    private Long contentId;
    // 点赞用户id
    private Long userId;
    // 用户是否已读（0为未读，1为已读）
    private Integer isRead;
    // 类型（枚举，like：点赞）
    private String type;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}