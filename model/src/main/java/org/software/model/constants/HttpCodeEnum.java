package org.software.model.constants;

import lombok.Getter;

@Getter
public enum HttpCodeEnum {
    // 成功与基础错误
    SUCCESS(200,"操作成功"),
    NEED_LOGIN(401,"需要登录后操作"),
    NO_OPERATOR_AUTH(403,"无权限操作"),
    SYSTEM_ERROR(500,"出现错误"),
    LOGIN_ERROR(505,"用户名或密码错误"),

    // 业务参数校验错误（新增）
    PARAM_NULL(40001,"参数不能为空"),
    TAG_NAME_NULL(40002,"标签名称不能为空"),
    TAG_ID_NULL(40003,"标签ID不能为空"),
    CONTENT_ID_NULL(40004,"内容ID不能为空"),
    TAG_IDS_NULL(40005,"标签ID列表不能为空"),
    USER_ID_NULL(40006,"用户ID不能为空"),
    TYPE_NULL(40007,"操作类型不能为空"),
    COMMENT_CONTENT_NULL(40008,"评论内容不能为空"),
    ROOT_COMMENT_ID_NULL(40009,"父评论ID存在时，根评论ID不能为空"),

    // 业务资源校验错误（新增）
    RESOURCE_NOT_EXIST(40401,"资源不存在或已删除"),
    RESOURCE_DUPLICATE(40901,"资源已存在"),
    TAG_NAME_DUPLICATE(40902,"标签名称已存在"),
    INVALID_TAG(40010,"存在无效或已删除的标签"),
    INVALID_TYPE(40011,"不支持的操作类型，仅支持'like'");

    final int code;
    final String msg;

    HttpCodeEnum(int code, String errorMessage){
        this.code = code;
        this.msg = errorMessage;
    }
}