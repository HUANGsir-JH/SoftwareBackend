package org.software.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.software.feign.NotificationFeignClient;
import org.software.model.Response;
import org.software.model.constants.FriendsConstants;
import org.software.model.constants.UserConstants;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageResult;
import org.software.model.social.FindFriendRequest;
import org.software.model.social.Friends;
import org.software.model.social.UnreadCounts;
import org.software.model.social.priv.PrivateConversations;
import org.software.model.user.User;
import org.software.model.user.UserStatusV;
import org.software.user.mapper.FriendsMapper;
import org.software.user.service.FriendsService;
import org.software.user.service.UserService;
import org.software.common.util.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
                UserConstants.USER_KEY + userId + ":" + FriendsConstants.FRIEND_KEY,
                Long.class,
                () -> {
                    QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id", userId).or().eq("friend_id", userId)
                            .eq("status", FriendsConstants.ACCEPTED);
                    HashSet<Object> set1 = new HashSet<>();
                    list(queryWrapper).forEach(friends -> {
                        // 添加双方ID
                        // set会自动去重
                        set1.add(friends.getUserId());
                        set1.add(friends.getFriendId());
                    });
                    // 移除自己的ID
                    set1.remove(userId);
                    return set1.stream().map(o -> (Long) o).collect(Collectors.toSet());
                }
        );

        // TODO: redis优化
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", set);
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

    @Transactional
    @Override
    public void addFriend(Long userId, Long friendId) throws BusinessException {
        if (redisHelper.isMember(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY, friendId)){
            throw new BusinessException("你们已经是好友了");
        }

        Friends friends = Friends.builder()
                .userId(userId > friendId ? friendId : userId)
                .friendId(userId > friendId ? userId : friendId)
                .status(FriendsConstants.PENDING)
                .requestedAt(DateTime.now())
                .build();

        save(friends);

        redisHelper.removeSet(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY);
    }

    @Override
    public List<User> findFriend(String query) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("nickname", query);
        return userService.list(queryWrapper);
    }

    @GlobalTransactional
    @Override
    public void agreeAddFriendRequest(Long userId, Long friendshipId) {
        UpdateWrapper<Friends> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", FriendsConstants.ACCEPTED)
                .eq("user_id", userId).or().eq("friend_id", userId)
                .eq("friendship_id", friendshipId)
                .ne("status", FriendsConstants.ACCEPTED);
        update(updateWrapper);

        redisHelper.removeSet(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY);

        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("friendship_id", friendshipId);
        Friends friends = getOne(queryWrapper);

        notificationFeignClient.addConv(Objects.equals(friends.getFriendId(), userId) ? friends.getUserId() : friends.getFriendId());
    }

    @Override
    public void rejectFriendRequest(Long userId, Long friendshipId) {
        UpdateWrapper<Friends> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", FriendsConstants.REJECTED)
                .eq("user_id", userId).or().eq("friend_id", friendshipId)
                .eq("friendship_id", friendshipId);
        update(updateWrapper);
    }

    @Transactional
    @Override
    public List<Friends> getNewFriendRequests(Long userId) {
        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).or().eq("friend_id", userId)
                .eq("status", FriendsConstants.PENDING);
        return list(queryWrapper);
    }

    @Override
    public String getFriendStatus(Long friendId) {
        long userId = StpUtil.getLoginIdAsLong();

        QueryWrapper<Friends> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId > friendId ? friendId : userId)
                .eq("friend_id", userId > friendId ? userId : friendId);
        Friends friends = getOne(queryWrapper);
        if (friends == null) {
            return null;
        } else {
            return friends.getStatus();
        }
    }

}

