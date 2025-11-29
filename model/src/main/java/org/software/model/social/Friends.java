package org.software.model.social;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 好友关系(Friends)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("friends")
@Builder
public class Friends {
    // 好友关系id
    @TableId(type = IdType.ASSIGN_ID)
    private Long friendshipId;
    // 发送加好友请求的用户id
    private Long userId;
    // 被添加的用户id
    private Long friendId;
    // 状态（如pending、accepted、rejected等）
    private String status;
    // 发送请求的时间
    private Date requestedAt;
    // 接收好友时间
    private Date acceptedAt;
    // 创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}