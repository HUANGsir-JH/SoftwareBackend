package org.software.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    public Module javaTimeModule() {
        // 创建一个简单的 Jackson Module
        SimpleModule module = new SimpleModule();

        // 当 Jackson 遇到 OffsetDateTime 类型时，使用我们自定义的序列化器
        module.addSerializer(OffsetDateTime.class, new CustomOffsetDateTimeSerializer());
        
        // 当 Jackson 遇到 Date 类型时，使用我们自定义的序列化器
        module.addSerializer(Date.class, new CustomDateSerializer());

        return module;
    }

    // 自定义的 OffsetDateTime 序列化器
    static class CustomOffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {

        // 目标时区（例如：北京时间）
        private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Shanghai");

        // 目标输出格式
        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public void serialize(
                OffsetDateTime value,
                JsonGenerator gen,
                SerializerProvider provider) throws IOException {

            if (value == null) {
                gen.writeNull();
                return;
            }

            // 1. 将 UTC/源时区 的时间转换为目标时区的时间
            String formatted = value
                    .atZoneSameInstant(TARGET_ZONE)
                    .format(FORMATTER);

            // 2. 输出格式化后的字符串
            gen.writeString(formatted);
        }
    }
    
    // 自定义的 Date 序列化器
    static class CustomDateSerializer extends JsonSerializer<Date> {
        
        private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
        private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Shanghai");

        @Override
        public void serialize(
                Date value,
                JsonGenerator gen,
                SerializerProvider provider) throws IOException {

            if (value == null) {
                gen.writeNull();
                return;
            }

            // 使用 SimpleDateFormat 格式化 Date
            SimpleDateFormat dateFormat = new SimpleDateFormat(PATTERN);
            dateFormat.setTimeZone(TIME_ZONE);
            String formatted = dateFormat.format(value);

            gen.writeString(formatted);
        }
    }
}