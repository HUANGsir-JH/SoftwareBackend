package org.software.model.content.media;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 内容媒体(ContentMedia)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("content_media")
public class ContentMedia {
    // 媒体文件Id
    @TableId
    private Integer mediaId;
    // 所属内容id
    private Integer contentId;
    // 文件存储路径
    private String fileUrl;
    // 媒体文件类型
    private String type;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}