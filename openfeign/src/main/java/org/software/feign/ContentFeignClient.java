package org.software.feign;

import org.software.model.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-content")
public interface ContentFeignClient {
    
    @GetMapping("/content/userdata")
    Response getUserContentData(@RequestParam("userId") Long userId);
}
