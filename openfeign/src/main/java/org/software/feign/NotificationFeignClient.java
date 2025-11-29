package org.software.feign;

import org.software.model.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("service-notification")
public interface NotificationFeignClient {

    @PostMapping("message/addConv")
    Response addConv(@RequestBody Long friendId);
}
