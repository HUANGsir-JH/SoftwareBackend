package org.software.model.interaction.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 子评论分页结果实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubCommentPageResult {
    // 当前第几页
    private Integer pageNum;
    // 当前分页大小
    private Integer pageSize;
    // 子评论列表
    private List<Comments> records;
}
