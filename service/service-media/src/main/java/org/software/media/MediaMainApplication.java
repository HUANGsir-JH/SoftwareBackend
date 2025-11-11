package org.software.media;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
public class MediaMainApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(MediaMainApplication.class, args);
    }
}
