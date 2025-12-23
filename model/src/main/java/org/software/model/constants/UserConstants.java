package org.software.model.constants;

public class UserConstants{
    // 用户状态常量
    public static final Integer USER_ACTIVE = 1;
    public static final Integer USER_BANNED = 0;

    // 用户性别常量
    public static final String MAN = "man";
    public static final String WOMAN = "woman";
    public static final String PRIVATE = "private";

    // 用户好友相关
    public static final String STATUS_MYSELF = "myself";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_BLOCKED = "blocked";

    public static final Long ADMIN_USER_ID = 1L;

    public static final String USER_KEY = "user:";
    public static final String FORGET_CODE_KEY = "forget:code:";
    public static final String FORGET_TOKEN_KEY = "forget:token:";
}