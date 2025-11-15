package org.software.model.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResult {
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private Object records;
}
