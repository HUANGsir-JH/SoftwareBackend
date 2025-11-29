package org.software.model.user;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

@Data
public class UserV {
    private Long userId;
    private String username;
    private String avatar;
    private String nickname;
}
