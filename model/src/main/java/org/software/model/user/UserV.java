package org.software.model.user;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserV {
    private Long userId;
    private String username;
    private String avatar;
    private String nickname;
    
    public void setUserV(UserStatusV user){
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.avatar = user.getAvatar();
        this.nickname = user.getNickname();
    }
}
