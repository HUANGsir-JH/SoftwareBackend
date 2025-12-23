package org.software.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.software.model.Response;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.constants.UserConstants;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.user.*;
import org.software.user.service.FriendsService;
import org.software.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private FriendsService friendsService;

    @GetMapping
    public Response getUser(Long userId) {
        User user = userService.getById(userId);
        UserStatusV userStatusV = new UserStatusV();
        userStatusV.setUser(user);
        userStatusV.setStatus(friendsService.getFriendStatus(userId));
        return Response.success(userStatusV);
    }

    @PutMapping
    public Response updateUser(@RequestBody UserUpdateD userD) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = User.builder()
                .userId(userId)
                .nickname(userD.getNickname())
                .signature(userD.getSignature())
                .sex(userD.getSex()).build();
        userService.updateById(user);
        return Response.success();
    }

    @PutMapping("/password")
    public Response updatePassword(@RequestBody PasswordView passV) {
        userService.updatePassword(passV);
        return Response.success();
    }

    @DeleteMapping
    public Response deleteUser(){
        Long userId = StpUtil.getLoginIdAsLong();
        userService.deleteUser(userId);
        StpUtil.logout();
        return Response.success();
    }

    @GetMapping("/search")
    public Response searchFriend(Integer pageNum, Integer pageSize, String query) {
        PageResult user = userService.searchFriend(pageNum, pageSize, query);
        return Response.success(user);
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
        boolean success = userService.updateById(user);
        if (success) {
            if (StpUtil.isLogin(userId)) {
                StpUtil.logout(userId);
            }
            return Response.success();
        }
        throw new BusinessException(HttpCodeEnum.BAN_USER_FAIL);
    }

    @PutMapping("/b/{userId}")
    public Response updateBannedUserStatus(@PathVariable String userId) {
        User user = User.builder()
                .userId(Long.valueOf(userId))
                .isActive(UserConstants.USER_ACTIVE)
                .build();
        boolean success = userService.updateById(user);
        if (success) {
            return Response.success();
        }
        throw new BusinessException(HttpCodeEnum.UNBAN_USER_FAIL);
    }



}
