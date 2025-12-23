package org.software.model.user;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusV {
    private Long userId;
    //用户名（登录用）
    private String username;
    //加密后的密码
    private String password;
    //注册邮箱
    private String email;
    //用户头像url
    private String avatar;
    //个性签名
    private String signature;
    private String nickname;
    //是否被ban，0为禁用，1为正常，默认1
    private Integer isActive;
    //背景图url
    private String backgroundImage;
    //性别，男，女，不透露
    private String sex;
    //注册时间
    private Date createdAt;
    //更新时间
    private Date updatedAt;
    //软删除字段，填写删除操作时间，但是真实删除数据。
    private Date deletedAt;
    private String status;
    
    public void setUser(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.email = user.getEmail();
        this.avatar = user.getAvatar();
        this.signature = user.getSignature();
        this.nickname = user.getNickname();
        this.isActive = user.getIsActive();
        this.backgroundImage = user.getBackgroundImage();
        this.sex = user.getSex();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.deletedAt = user.getDeletedAt();
    }
}
