package org.software.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSimpleDto {
    private Long userId;
    private String username;
    private String avatar;
    private String nickname;
}
