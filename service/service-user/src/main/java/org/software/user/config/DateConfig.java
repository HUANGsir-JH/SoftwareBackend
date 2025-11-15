package org.software.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class DateConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // LocalDateTime
        registry.addConverter(new Converter<String, LocalDateTime>() {
            public LocalDateTime convert(String s) {
                if (s == null || s.isBlank()) return null;
                return LocalDateTime.parse(s.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        });
        // LocalDate（如需要）
        registry.addConverter(new Converter<String, LocalDate>() {
            public LocalDate convert(String s) {
                if (s == null || s.isBlank()) return null;
                return LocalDate.parse(s.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        });
    }
}