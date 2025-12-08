package org.software.model.content;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 标签(Tag)表实体类*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tag")
public class Tag {
    // 标签id
    @TableId(type = IdType.ASSIGN_ID)
    private Integer tagId;
    // 标签名称
    private String tagName;
    // 是否启用（0为禁用，1为启用）
    private Integer isActive;
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