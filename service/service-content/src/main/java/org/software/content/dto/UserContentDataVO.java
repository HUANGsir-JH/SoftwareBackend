package org.software.content.dto;

import lombok.Data;

@Data
public class UserContentDataVO {
    private Integer totalLike;
    private Integer totalFavorite;

    public UserContentDataVO(int i, int i1) {
        totalLike = i;
        totalFavorite = i1;
    }
}
