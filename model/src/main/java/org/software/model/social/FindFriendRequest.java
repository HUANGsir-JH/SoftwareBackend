package org.software.model.social;

import lombok.Data;

@Data
public class FindFriendRequest {
    private Long id;
    private String username;
    private String nickname;
}
