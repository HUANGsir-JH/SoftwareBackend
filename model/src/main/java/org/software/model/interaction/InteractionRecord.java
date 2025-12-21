package org.software.model.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.software.model.user.User;

import java.util.Date;

/**
 * 点赞/收藏记录(InteractionRecord)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteractionRecord {
    // 点赞记录id
    @TableId(type = IdType.AUTO)
    private Integer likeId;
    // 被点赞的内容id
    private Integer contentId;
    // 首条媒体信息
    private String firstMedia;
    // 媒体类型
    private String mediaType;
    // 点赞用户id
    private Integer userId;
    // 点赞用户信息
    private User user;
    // 用户是否已读（0为未读，1为已读）
    private Integer isRead;
    // 类型（枚举，like：点赞，collect：收藏）
    private String type;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}

