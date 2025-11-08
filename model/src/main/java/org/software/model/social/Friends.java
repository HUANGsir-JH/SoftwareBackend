package org.software.model.social;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
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
public class Friends {
    // 好友关系id
    @TableId
    private Integer friendshipId;
    // 发送加好友请求的用户id
    private Integer userId;
    // 被添加的用户id
    private Integer friendId;
    // 状态（如pending、accepted、rejected等）
    private String status;
    // 发送请求的时间
    private Date requestedAt;
    // 接收好友时间
    private Date acceptedAt;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}