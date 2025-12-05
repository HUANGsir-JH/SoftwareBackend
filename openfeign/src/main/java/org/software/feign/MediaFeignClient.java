package org.software.feign;

import org.software.model.Response;
import org.software.model.media.UploadD;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-media")
public interface MediaFeignClient {

    @PostMapping("/upload/signature")
    Response upload(@RequestBody UploadD uploadD);
}
