package org.software.model.social.group;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 群组邀请(GroupInvitations)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("group_invitations")
public class GroupInvitations {
    // 邀请记录id
    @TableId
    private Integer invitationId;
    // 群组id
    private Integer groupId;
    // 邀请人id
    private Integer inviterId;
    // 被邀请的人id
    private Integer inviteeId;
    // 邀请状态
    private String status;
    // 被邀请者接收时间
    private Date responsedAt;
    // 管理员/群主批准时间
    private Date approvedAt;
    // 过期时间
    private Date expiredAt;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}