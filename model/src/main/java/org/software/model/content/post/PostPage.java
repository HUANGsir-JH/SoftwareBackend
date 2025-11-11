package org.software.model.content.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 帖子分页结果实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostPage {
    // 帖子总数
    private Integer total;
    // 当前第几页
    private Integer pageNum;
    // 当前分页大小
    private Integer pageSize;
    // 帖子列表
    private List<PostE> records;
}