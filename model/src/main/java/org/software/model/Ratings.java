package org.software.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评分(Ratings)表实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("ratings")
public class Ratings {
    // 评分id
    @TableId
    private Integer ratingId;
    // 被评分的内容id
    private Integer contentId;
    // 评分者id
    private Integer userId;
    // 评分（1~5，或其他分值范围）
    private Double score;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 软删除字段
    private Date deletedAt;
}