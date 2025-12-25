package org.software.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDataV {
    Integer totalFriend;
    Integer totalLike;
    Integer totalFavorite;
}
