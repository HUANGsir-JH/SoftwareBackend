package org.software.model.constants;

import lombok.Getter;

@Getter
public enum HttpCodeEnum {
    // 成功
    SUCCESS(200,"操作成功"),
    NEED_LOGIN(401,"需要登录后操作"),
    NO_OPERATOR_AUTH(403,"无权限操作"),
    SYSTEM_ERROR(500,"出现错误"),
    LOGIN_ERROR(505,"用户名或密码错误");

    final int code;
    final String msg;

    HttpCodeEnum(int code, String errorMessage){
        this.code = code;
        this.msg = errorMessage;
    }

}