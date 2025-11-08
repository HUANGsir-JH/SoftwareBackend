package org.software.model.group;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 群组(Groups)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("groups")
public class Groups {
    // 群聊唯一id
    @TableId
    private Integer groupId;
    // 群聊名称
    private String name;
    // 群头像url
    private String avatarUrl;
    // 群简介
    private String description;
    // 创建人id
    private Integer creatorId;
    // 群公告
    private String announcement;
    // 群公告更新时间
    private Date announcementUpdatedAt;
    // 最大成员数
    private Integer maxMembers;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}