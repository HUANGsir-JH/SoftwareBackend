package org.software.model.constants;

public class MessageConstants {

    public static final String CONVERSATION_KEY = "conversation:";

    public static final Integer IS_READ = 1;
    public static final Integer NOT_READ = 0;

    // 文件类型：'text', 'image', 'video', 'file'
    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String FILE = "file";

    // 消息去重
    public static final String MESSAGE_DEDUP_KEY = "msg:dedup:"; // 消息去重 key前缀
    public static final long MESSAGE_DEDUP_TIMEOUT = 5; // 消息去重过期时间（5分钟）
}
