package org.software.feign;

import org.software.model.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "service-user")
public interface UserFeignClient {

    @GetMapping("/user")
    Response getUser(Long userId);
}
