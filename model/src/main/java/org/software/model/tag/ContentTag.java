package org.software.model.tag;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 内容标签关联(ContentTag)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("content_tag")
public class ContentTag {
    // 内容Id
    private Integer contentId;
    // 标签Id
    private Integer tagId;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}