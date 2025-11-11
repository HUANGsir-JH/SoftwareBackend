package org.software.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(value = "service-media")
public interface MediaFeignClient {

    @PutMapping("/upload/avatar")
    String uploadAvatar(MultipartFile file, Integer UserId);
}
