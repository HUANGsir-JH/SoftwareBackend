package org.software.media.util;

import lombok.extern.slf4j.Slf4j;
import org.software.media.config.R2Config;
import org.software.model.constants.MediaContants;
import org.software.model.content.media.UploadD;
import org.software.model.content.media.UploadV;
import org.software.model.exception.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.io.InputStream;
import java.time.Duration;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Resource to OSS Utility
 * 针对不同业务场景（头像、动态、资料）提供安全、便捷的文件上传前置校验
 */
@Slf4j
@Component
public class R2OSSUtil {

    @Autowired
    private R2Config r2Config;
    @Autowired
    private S3Client s3Client;
    @Autowired
    private S3Presigner s3Presigner;

    // ========== 文件大小限制（单位：字节） ==========
    public static final long AVATAR_MAX_SIZE = 5 * 1024 * 1024;        // 5MB
    public static final long FEED_MEDIA_MAX_SIZE = 50 * 1024 * 1024;    // 50MB
    public static final long GENERAL_DOC_MAX_SIZE = 100 * 1024 * 1024;  // 100MB

    // ========== MIME 白名单 ==========
    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
            // 排除 svg（可能含脚本），如需可加回
    );

    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/quicktime",        // .mov
            "video/x-matroska",       // .mkv
            "video/webm"
            // 通常动态只支持主流格式
    );

    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",       // .xlsx
            "text/plain",
            "text/csv"
    );

    // ========== 文件扩展名映射（用于 objectKey 生成） ==========
    private static final Map<String, String> MIME_TO_EXT = Map.ofEntries(
            Map.entry("image/jpeg", "jpg"),
            Map.entry("image/png", "png"),
            Map.entry("image/gif", "gif"),
            Map.entry("image/webp", "webp"),
            Map.entry("video/mp4", "mp4"),
            Map.entry("video/quicktime", "mov"),
            Map.entry("video/x-matroska", "mkv"),
            Map.entry("video/webm", "webp"),
            Map.entry("application/pdf", "pdf"),
            Map.entry("text/plain", "txt"),
            Map.entry("text/csv", "csv")
            // 可继续扩展
    );

    // ========== 辅助：提取扩展名 ==========
    private static final Pattern EXT_PATTERN = Pattern.compile(".*\\.([^.]+)$");

    private static String extractExtension(String filename) {
        if (filename == null) return "";
        var matcher = EXT_PATTERN.matcher(filename.toLowerCase());
        return matcher.matches() ? matcher.group(1) : "";
    }

    // ========== 核心校验方法 ==========
    private static void validate(
            UploadD uploadD,
            Set<String> allowedMimeTypes,
            long maxSizeBytes,
            String scenarioName
    ) {
        if (uploadD == null) {
            throw new IllegalArgumentException("[" + scenarioName + "] 文件不能为空");
        }

        String contentType = uploadD.getContentType();
        String originalFilename = uploadD.getFileName();
        long size = uploadD.getContentLength();

        // 1. 大小校验
        if (size > maxSizeBytes) {
            throw new IllegalArgumentException(
                    String.format("[%s] 文件大小不能超过 %d MB，当前: %.2f MB",
                            scenarioName,
                            maxSizeBytes / (1024 * 1024),
                            size / (1024.0 * 1024.0))
            );
        }

        // 2. MIME 类型校验
        if (contentType == null || contentType.isBlank()) {
            throw new IllegalArgumentException("[" + scenarioName + "] 无法识别文件类型");
        }

        contentType = contentType.toLowerCase();
        if (!allowedMimeTypes.contains(contentType)) {
            throw new IllegalArgumentException(
                    String.format("[%s] 不支持的文件类型: %s，仅支持: %s",
                            scenarioName,
                            contentType,
                            allowedMimeTypes)
            );
        }

        // 3. 扩展名与 MIME 一致性校验（防伪造）
        String ext = extractExtension(originalFilename);
        String expectedExt = MIME_TO_EXT.get(contentType);
        if (expectedExt != null && !expectedExt.equals(ext)) {
            // 允许部分别名（如 jpeg/jpg）
            if (!(expectedExt.equals("jpg") && ext.equals("jpeg")) &&
                    !(expectedExt.equals("jpeg") && ext.equals("jpg"))) {
                throw new IllegalArgumentException(
                        String.format("[%s] 文件扩展名 (%s) 与实际类型 (%s) 不匹配",
                                scenarioName, ext, contentType));
            }
        }
    }

    // ========== 场景1：用户头像/背景图片（仅图片） ==========
    public static String generateAvatarObjectKey(UploadD uploadD, Long userId) {
        validate(uploadD, IMAGE_TYPES, AVATAR_MAX_SIZE, "用户头像/背景图片");

        String CT = Objects.requireNonNull(uploadD.getContentType()).toLowerCase();
        String ext = MIME_TO_EXT.getOrDefault(CT, "jpg"); // 默认 jpg

        // 安全路径：avatars/{userId}/{timestamp}_{uuid}.{ext}
        return String.format("images/%s/%d_%s.%s",
                userId,
                System.currentTimeMillis(),
                UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                ext
        );
    }

    // ========== 场景2：用户动态（图片或短视频） ==========
    public static String generateFeedMediaObjectKey(UploadD uploadD, Long userId) {
        Set<String> allowed = new HashSet<>(IMAGE_TYPES);
        allowed.addAll(VIDEO_TYPES);
        allowed.addAll(IMAGE_TYPES);
        validate(uploadD, allowed, FEED_MEDIA_MAX_SIZE, "用户动态");

        String contentType = Objects.requireNonNull(uploadD.getContentType()).toLowerCase();
        String ext = MIME_TO_EXT.getOrDefault(contentType, "bin");

        // 路径：feeds/{userId}/{timestamp}_{uuid}.{ext}
        return String.format("feeds/%s/%d_%s.%s",
                userId,
                System.currentTimeMillis(),
                UUID.randomUUID().toString().replace("-", "").substring(0, 8),
                ext
        );
    }

    // =============================================================================================

    /**
     * 上传文件流到 R2，并返回可公开访问的 URL。
     *
     * @param inputStream 要上传的文件输入流
     * @param objectName  在存储桶中存储的对象名称 (例如 "data/report.pdf")。不允许为 null 或空。
     * @param contentLength 输入流的长度。对于 InputStream，提供长度可以优化上传。
     * @param contentType 文件的 MIME 类型 (例如 "image/jpeg", "application/pdf")。
     * @return 上传成功后文件的公开访问 URL，失败则返回 null。
     */
    public String doUpload(InputStream inputStream, String objectName, long contentLength, String contentType) {
        log.info("开始执行上传任务，目标对象：{}", objectName);
        if (inputStream == null) {
            log.error("上传失败：输入流不能为空");
            return null;
        }
        if (objectName == null || objectName.trim().isEmpty()) {
            log.error("上传失败：对象名称不能为空");
            return null;
        }
        if (contentType == null || contentType.trim().isEmpty()) {
            log.warn("警告：{}的文件格式为空", objectName);
            contentType = "application/octet-stream";
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Config.getBucket())
                .key(objectName)
                .contentType(contentType)
                // .acl(ObjectCannedACL.PUBLIC_READ) // 如果你的 R2 存储桶策略允许，并且你想设置为公共可读
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
            log.info("File uploaded successfully to R2: {}/{}", r2Config.getBucket(), objectName);

            // 构建公开访问 URL
            // 确保 domain 不以 / 结尾，objectName 不以 / 开头，以避免双斜杠
            String domain = r2Config.getDomain();
            if (domain.endsWith("/")) {
                domain = domain.substring(0, domain.length() - 1);
            }
            String keyForUrl = objectName;
            if (keyForUrl.startsWith("/")) {
                keyForUrl = keyForUrl.substring(1);
            }
            // 通常 R2 的公开访问域名会自动处理 https，如果你的 domain 配置不带协议，可能需要添加
            // 例如，如果 domain 是 "myr2.example.com"，则 URL 是 "https://myr2.example.com/objectKey"
            // 如果 domain 已经是 "https://myr2.example.com"，则直接拼接
            if (!domain.toLowerCase().startsWith("http://") && !domain.toLowerCase().startsWith("https://")) {
                domain = "https://" + domain;
            }

            return domain + "/" + keyForUrl;

        } catch (S3Exception e) {
            log.error("Error uploading file to R2: {} (AWS Error Code: {})", e.awsErrorDetails().errorMessage(), e.awsErrorDetails().errorCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error uploading file to R2: {}", e.getMessage(), e);
        } finally {
            // 注意：此方法不负责关闭传入的 inputStream，调用者应负责关闭
        }
        return null;
    }

    public String upload(MultipartFile file, Long userId, String scenario) throws SystemException {
        UploadD uploadD = UploadD.builder()
                .fileName(file.getOriginalFilename())
                .contentLength(file.getSize())
                .contentType(file.getContentType())
                .build();

        String filename = switch (scenario.toLowerCase()) {
            case "avatar" -> generateAvatarObjectKey(uploadD, userId);
            case "feed" -> generateFeedMediaObjectKey(uploadD, userId);
            default -> throw new IllegalArgumentException("不支持的场景类型: " + scenario);
        };
        long size = file.getSize();
        String contentType = file.getContentType();
        try (InputStream inputStream = file.getInputStream()){
            String fileUrl = doUpload(inputStream, filename, size, contentType);
            if (fileUrl != null) {
                log.info("文件上传成功: {}，访问URL: {}", filename, fileUrl);
                return fileUrl;
            } else {
                log.error("文件上传失败: {}，R2OssUtil未能返回URL。", filename);
                // R2OssUtil 内部应该已经记录了更详细的S3Exception日志
                throw new SystemException(filename);
            }
        }catch (Exception e){
            log.error("Unexpected error uploading file to R2: {}", e.getMessage(), e);
            throw new SystemException(filename);
        }
    }

    // =====================================================================================================================

    /**
     * 生成上传预签名URL
     * 客户端可使用此URL直接上传文件到OSS,避免文件流经服务器
     *
     * @param objectKey   对象键(文件路径)
     * @param expirationMinutes 过期时间(分钟),建议5-60分钟
     * @return 预签名上传URL
     */
    private String generatePresignedPutUrl(
            String objectKey,
            int expirationMinutes
    ) {
        String bucketName = r2Config.getBucket();
        if (bucketName == null || objectKey == null) {
            throw new IllegalArgumentException("presigner、bucketName 和 objectKey 不能为空");
        }
        if (expirationMinutes < 1 || expirationMinutes > 1440) {
            throw new IllegalArgumentException("过期时间必须在1-1440分钟之间");
        }

        PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey);

//        // 如果指定了contentType,则限制上传类型
//        if (contentType != null && !contentType.isBlank()) {
//            requestBuilder.contentType(contentType);
//        }

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes))
                .putObjectRequest(requestBuilder.build())
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /**
     * 便捷方法：为已验证的文件生成上传预签名URL
     * 自动调用对应场景的校验+ObjectKey生成+预签名URL生成
     *
     * @param userId     用户ID
     * @param uploadD   上传描述对象
     * @param expirationMinutes 过期时间(分钟)
     * @return 包含objectKey和预签名URL的Map
     */
    public UploadV generatePresignedUploadInfo(
            Long userId,
            UploadD uploadD,
            int expirationMinutes
    ) {
        String objectKey = switch (uploadD.getType().toLowerCase()) {
            case "images" -> generateAvatarObjectKey(uploadD, userId);
            case "feeds" -> generateFeedMediaObjectKey(uploadD, userId);
            default -> throw new IllegalArgumentException("不支持的场景类型: " + uploadD.getType());
        };

        String presignedUrl = generatePresignedPutUrl(
                objectKey,
                expirationMinutes
        );

        return UploadV.builder()
                .objectKey (objectKey)
                .presignedUrl(presignedUrl)
                .expiresIn(expirationMinutes + " minutes")
                .build();
    }
    
}