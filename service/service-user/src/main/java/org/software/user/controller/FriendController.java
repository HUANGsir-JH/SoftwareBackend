package org.software.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.software.model.Response;
import org.software.model.social.FindFriendRequest;
import org.software.model.social.Friends;
import org.software.model.social.SendMessageRequest;
import org.software.model.user.User;
import org.software.user.service.FriendsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 处理好友相关接口
 */
@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendsService friendsService;

    /**
     * 获取好友列表
     */
    @GetMapping
    public Response getFriendList() {
        Long userId = StpUtil.getLoginIdAsLong();
        Set<User> friends = friendsService.listFriends(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("total", friends.size());
        data.put("friends", friends);
        return Response.success(data);
    }

    /**
     * 添加好友
     */
    @PostMapping
    public Response addFriend(@RequestBody Long friendId) {
        Long userId = StpUtil.getLoginIdAsLong();
        friendsService.addFriend(userId, friendId);
        return Response.success();
    }

    /**
     * 删除好友
     */
    @DeleteMapping("/{friendId}")
    public Response deleteFriend(@PathVariable String friendId) {
        Long userId = StpUtil.getLoginIdAsLong();
        friendsService.delFriend(userId, Long.valueOf(friendId));
        return Response.success();
    }

    /**
     * 查找好友
     */
    @GetMapping("/find")
    public Response findFriend(@RequestParam FindFriendRequest request) {
        List<User> friend = friendsService.findFriend(request);
        return Response.success(friend);
    }

    /**
     * 获取添加好友请求
     */
    @GetMapping("/new")
    public Response getNewFriendRequests() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Friends> friendsReq = friendsService.getNewFriendRequests(userId);
        return Response.success(friendsReq);
    }

    /**
     * 同意添加好友请求
     */
    @PutMapping("/new/{friendshipId}")
    public Response agreeAddFriend(@PathVariable Long friendshipId) {
        Long userId = StpUtil.getLoginIdAsLong();
        friendsService.agreeAddFriendRequest(userId, friendshipId);
        return Response.success();
    }

    /**
     * 拒绝添加好友请求
     */
    @DeleteMapping("/new/{friendshipId}")
    public Response rejectFriendRequest(@PathVariable Long friendshipId) {
        Long userId = StpUtil.getLoginIdAsLong();
        friendsService.rejectFriendRequest(userId, friendshipId);
        return Response.success();
    }
}
