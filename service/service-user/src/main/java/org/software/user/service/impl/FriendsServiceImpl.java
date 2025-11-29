package org.software.user.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.software.feign.NotificationFeignClient;
import org.software.model.constants.FriendsConstants;
import org.software.model.constants.UserConstants;
import org.software.model.social.FindFriendRequest;
import org.software.model.social.Friends;
import org.software.model.social.priv.PrivateConversations;
import org.software.model.user.User;
import org.software.user.mapper.FriendsMapper;
import org.software.user.service.FriendsService;
import org.software.user.service.UserService;
import org.software.common.util.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 好友关系表(Friends)表服务实现类
 *
 * @author Ra1nbot
 * @since 2025-11-26 22:13:49
 */
@Service
public class FriendsServiceImpl extends ServiceImpl<FriendsMapper, Friends> implements FriendsService {

    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private UserService userService;
    @Autowired
    private NotificationFeignClient notificationFeignClient;

    @Override
    public Set<User> listFriends(Long userId) {
        Set<Long> set = redisHelper.getSet(
                UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY,
                Long.class,
                () -> {
                    QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id", userId)
                            .eq("status", FriendsConstants.ACCEPTED);
                    return list(queryWrapper).stream()
                            .map(Friends::getFriendId)
                            .collect(Collectors.toSet());
                }
        );

        // TODO: redis优化
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", set);
        return new HashSet<>(userService.list(queryWrapper));
    }

    @Override
    public void delFriend(Long userId, Long friendId) {
        UpdateWrapper<Friends> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("delete_at", DateTime.now())
                .eq("user_id", userId)
                .eq("friend_id", friendId)
                .eq("status", FriendsConstants.ACCEPTED);
        update(updateWrapper);

        redisHelper.removeSet(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY);
    }

    @GlobalTransactional
    @Override
    public void addFriend(Long userId, Long friendId) {
        Friends friends = Friends.builder()
                .userId(userId)
                .friendId(friendId)
                .status(FriendsConstants.PENDING)
                .requestedAt(DateTime.now())
                .build();

        save(friends);

        redisHelper.removeSet(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY);

        notificationFeignClient.addConv(friendId);
    }

    @Override
    public List<User> findFriend(FindFriendRequest request) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", request.getId())
                .eq("username", request.getUsername())
                .eq("nickname", request.getNickname());

        return userService.list(queryWrapper);
    }

    @Override
    public void agreeAddFriendRequest(Long userId, Long friendshipId) {
        UpdateWrapper<Friends> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", FriendsConstants.ACCEPTED)
                .eq("user_id", userId)
                .eq("friendship_id", friendshipId)
                .ne("status", FriendsConstants.ACCEPTED);
        update(updateWrapper);

        redisHelper.removeSet(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY);
    }

    @Override
    public void rejectFriendRequest(Long userId, Long friendshipId) {
        UpdateWrapper<Friends> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", FriendsConstants.REJECTED)
                .eq("user_id", userId)
                .eq("friendship_id", friendshipId);
        update(updateWrapper);
    }

    @Override
    public List<Friends> getNewFriendRequests(Long userId) {
        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return list(queryWrapper);
    }
}

