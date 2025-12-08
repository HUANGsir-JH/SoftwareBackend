package org.software.model.content;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    // 更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    // 软删除字段
    @TableLogic(value = "null", delval = "now()")
    private Date deletedAt;
}