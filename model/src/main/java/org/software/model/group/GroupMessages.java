package org.software.model.group;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 群组消息(GroupMessages)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("group_messages")
public class GroupMessages {
    // 消息id
    @TableId
    private Integer messageId;
    // 消息所属群聊id
    private Integer groupId;
    // 发送者id
    private Integer senderId;
    // 消息类型
    private String type;
    // text类型的内容
    private String content;
    // image/video/文件的存储路径
    private String fileUrl;
    // 回复的消息id
    private Integer repliedToMessageId;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}