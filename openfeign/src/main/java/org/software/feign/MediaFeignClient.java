package org.software.feign;

import org.software.model.Response;
import org.software.model.content.media.UploadD;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(value = "service-media")
public interface MediaFeignClient {

    @PostMapping("/upload/signature")
    Response upload(@RequestBody UploadD uploadD);
}
