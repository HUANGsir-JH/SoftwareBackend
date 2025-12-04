package org.software.content;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.software.feign")
@ComponentScan(basePackages = "org.software")
public class ContentMainApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(ContentMainApplication.class, args);
    }
}
