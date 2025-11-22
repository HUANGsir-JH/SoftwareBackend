package org.software.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.software.model.Response;
import org.software.model.constants.UserConstants;
import org.software.model.content.media.UploadD;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.user.PageUserD;
import org.software.model.user.PasswordView;
import org.software.model.user.User;
import org.software.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Response getUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Response.success(userService.getById(userId));
    }

    @PutMapping
    public Response updateUser(@RequestBody User user) {
        Long userId = StpUtil.getLoginIdAsLong();
        user.setUserId(userId);
        if (user.getPassword() != null) {
            user.setPassword(null); // 禁止通过此接口修改密码
        }
        userService.updateById(user);
        return Response.success();
    }

    @PutMapping("/password")
    public Response updatePassword(@RequestBody PasswordView passV) throws BusinessException {
        userService.updatePassword(passV);
        return Response.success();
    }

    @PostMapping("/avatar")
    public Response uploadAvatar(@RequestBody UploadD uploadD) throws BusinessException {
        String url = userService.updateAvatar(uploadD);
        return Response.success(url);
    }

    @GetMapping("/backImage")
    public Response uploadBackImage(@RequestBody UploadD uploadD) throws BusinessException {
        String url = userService.updateBG(uploadD);
        return Response.success(url);
    }

// ========================= B端 ==============================

    @GetMapping("/b")
    public Response getUserBatch(PageQuery pageQuery, PageUserD pageUserD) {
        PageResult pageResult = userService.bPage(pageQuery, pageUserD);
        return Response.success(pageResult);
    }

    @DeleteMapping("/b/{userId}")
    public Response banUser(@PathVariable String userId) {
        User user = User.builder()
                .userId(Long.valueOf(userId))
                .isActive(UserConstants.USER_BANNED)
                .build();
        userService.updateById(user);
        return Response.success();
    }

    @PutMapping("/b/{userId}")
    public Response updateBannedUserStatus(@PathVariable String userId) {
        User user = User.builder()
                .userId(Long.valueOf(userId))
                .isActive(UserConstants.USER_ACTIVE)
                .build();
        userService.updateById(user);
        return Response.success();
    }



}
