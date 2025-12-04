package org.software.notification;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients(basePackages = "org.software.feign")
@ComponentScan(basePackages = "org.software")
public class NotificationMainApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(NotificationMainApplication.class, args);
    }
}
