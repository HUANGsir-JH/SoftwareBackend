package org.software.model.constants;

import lombok.Getter;

@Getter
public enum HttpCodeEnum {
    // 成功与基础错误
    SUCCESS(200, "操作成功"),
    PARAM_ERROR(400, "请求参数错误"),
    NEED_LOGIN(401, "需要登录后操作"),
    NO_OPERATOR_AUTH(403, "无权限操作"),
    RESOURCE_NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "服务器开小差了，请稍后再试吧"),

    // 10xxxx 用户服务
    LOGIN_ERROR(101001, "用户名或密码错误"),
    USER_NOT_EXIST(101002, "用户不存在"),
    USER_DISABLED(101003, "用户已被禁用"),
    REGISTERED(101004, "邮箱或用户名已被注册"),
    PASSWORD_NOT_MATCH(101007, "两次输入的密码不一致"),
    OLD_AND_NEW_PASSWORD_SAME(101008, "新密码不能与旧密码相同"),
    UPLOAD_PRESIGNED_URL_FAILED(101009, "生成预签名URL失败"),

    // 20xxxx 内容服务
    // 201xxx 评论服务
    COMMENT_NOT_FOUND(201001, "评论不存在"),
    COMMENT_ADDED_SUCCESS(201002, "评论添加成功"),
    PARENT_COMMENT_NOT_FOUND(201003, "父评论不存在"),
    INVALID_TYPE(201004, "不支持的操作类型"),
    
    // 202xxx 标签服务
    TAG_NAME_DUPLICATE(202001, "标签名称已存在"),
    INVALID_TAG(202002, "无效的标签"),

    // 30xxxx 媒体服务
    // 301xxx 文件验证
    FILE_NULL(301001, "文件不能为空"),
    FILE_SIZE_EXCEEDED(301002, "文件大小超出限制"),
    FILE_TYPE_NOT_RECOGNIZED(301003, "无法识别文件类型"),
    FILE_TYPE_NOT_SUPPORTED(301004, "不支持的文件类型"),
    FILE_EXTENSION_MISMATCH(301005, "文件扩展名与实际类型不匹配"),
    
    // 302xxx 文件操作
    OBJECT_NAME_NULL(302001, "对象名称不能为空"),
    INPUT_STREAM_NULL(302002, "输入流不能为空"),
    BUCKET_NAME_NULL(302003, "存储桶名称不能为空"),
    EXPIRATION_INVALID(302004, "过期时间无效"),
    FILE_UPLOAD_FAILED(302005, "文件上传失败"),
    PRESIGNED_URL_GENERATE_FAILED(302006, "生成预签名URL失败");

    final int code;
    final String msg;

    HttpCodeEnum(int code, String errorMessage){
        this.code = code;
        this.msg = errorMessage;
    }
}