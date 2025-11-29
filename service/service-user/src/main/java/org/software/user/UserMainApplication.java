package org.software.user;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.software.feign")
@ComponentScan(basePackages = "org.software.common")
public class UserMainApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(UserMainApplication.class, args);
    }
}
