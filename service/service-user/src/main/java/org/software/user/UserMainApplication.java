package org.software.user;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.software.feign")
public class UserMainApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(UserMainApplication.class, args);
    }
}
