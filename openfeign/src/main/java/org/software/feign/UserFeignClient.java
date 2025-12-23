package org.software.feign;

import org.software.model.Response;
import org.software.model.page.PageResult;
import org.software.model.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "service-user")
public interface UserFeignClient {

    @GetMapping("/user")
    Response getUser(@RequestParam("userId") Long userId);

    @GetMapping("/user/search")
    Response searchFriend(@RequestParam("pageNum") Integer pageNum,@RequestParam("pageSize") Integer pageSize,@RequestParam("query") String query);

}