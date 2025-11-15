package org.software.model.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageQuery {
    // 分页参数
    private Integer pageNum = 1;     // 当前页码，默认第1页
    private Integer pageSize = 10;   // 每页大小，默认10条

    // 时间范围
    private LocalDateTime startTime; // 创建/更新开始时间
    private LocalDateTime endTime;   // 创建/更新结束时间
}
