package org.software.model.social.group;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 群组成员(GroupMembers)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("group_members")
public class GroupMembers {
    // 成员关系id
    @TableId
    private Integer membershipId;
    // 群组id
    private Integer groupId;
    // 用户id
    private Integer userId;
    // 角色（普通成员、管理员等）
    private String role;
    // 加入时间
    private Date joinedAt;
    // 离开时间
    private Date leftAt;
    // 是否被禁言（0为未禁言，1为禁言）
    private Integer isBanned;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}