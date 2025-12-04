package org.software.model.user;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户表(User)表实体类
 *
 * @author Ra1nbot
 * @since 2025-11-08 09:58:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
@Builder
public class User {
    //用户唯一Id
    @TableId(type = IdType.ASSIGN_ID)
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
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
    //软删除字段，填写删除操作时间，但是真实删除数据。
    @TableLogic(value = "null", delval = "now()")
    private Date deletedAt;
}

