package org.software.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageUserDTO {
    private String username;         // 用户名
    private String email;            // 邮箱
    private String sex;              // 性别
    private Boolean isActive;        // 是否激活
}
