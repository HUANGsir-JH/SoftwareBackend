package org.software.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.social.FindFriendRequest;
import org.software.model.social.Friends;
import org.software.model.user.User;

import java.util.List;
import java.util.Set;


/**
 * 好友关系表(Friends)表服务接口
 *
 * @author Ra1nbot
 * @since 2025-11-26 22:13:49
 */
public interface FriendsService extends IService<Friends> {

    Set<User> listFriends(Long userId);

    void agreeAddFriendRequest(Long userId, Long friendshipId);

    void delFriend(Long userId, Long friendId);

    void addFriend(Long userId, Long friendId) throws BusinessException;

    List<User> findFriend(String query);

    void rejectFriendRequest(Long userId, Long friendshipId);

    List<Friends> getNewFriendRequests(Long userId);

    String getFriendStatus(Long userId);
}

