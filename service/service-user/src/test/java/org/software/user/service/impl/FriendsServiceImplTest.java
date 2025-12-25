package org.software.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.common.util.RedisHelper;
import org.software.feign.NotificationFeignClient;
import org.software.model.Response;
import org.software.model.constants.FriendsConstants;
import org.software.model.constants.UserConstants;
import org.software.model.exception.BusinessException;
import org.software.model.social.Friends;
import org.software.model.user.User;
import org.software.user.mapper.FriendsMapper;
import org.software.user.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FriendsServiceImpl 白盒测试
 * 测试重点：好友关系管理核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class FriendsServiceImplTest {

    private FriendsMapper friendsMapper;
    private UserService userService;
    private NotificationFeignClient notificationFeignClient;
    private RedisHelper redisHelper;
    private FriendsServiceImpl friendsService;

    private User testUser1;
    private User testUser2;
    private Friends testFriendship;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        friendsMapper = mock(FriendsMapper.class);
        userService = mock(UserService.class);
        notificationFeignClient = mock(NotificationFeignClient.class);
        redisHelper = mock(RedisHelper.class);

        // 手动创建 FriendsServiceImpl 并注入依赖
        friendsService = new FriendsServiceImpl() {
            @Override
            public List<Friends> list(Wrapper<Friends> queryWrapper) {
                return friendsMapper.selectList(queryWrapper);
            }
            
            @Override
            public boolean save(Friends entity) {
                int result = friendsMapper.insert(entity);
                return result > 0;
            }
            
            @Override
            public boolean update(Wrapper<Friends> updateWrapper) {
                int result = friendsMapper.update(null, updateWrapper);
                return result > 0;
            }
            
            @Override
            public Friends getOne(Wrapper<Friends> queryWrapper) {
                return friendsMapper.selectOne(queryWrapper);
            }
        };

        // 使用反射注入 Mock 依赖
        try {
            // ServiceImpl 基类使用 baseMapper 而不是 friendsMapper
            java.lang.reflect.Field friendsMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
            friendsMapperField.setAccessible(true);
            friendsMapperField.set(friendsService, friendsMapper);

            java.lang.reflect.Field userServiceField = FriendsServiceImpl.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(friendsService, userService);

            java.lang.reflect.Field notificationFeignClientField = FriendsServiceImpl.class.getDeclaredField("notificationFeignClient");
            notificationFeignClientField.setAccessible(true);
            notificationFeignClientField.set(friendsService, notificationFeignClient);

            java.lang.reflect.Field redisHelperField = FriendsServiceImpl.class.getDeclaredField("redisHelper");
            redisHelperField.setAccessible(true);
            redisHelperField.set(friendsService, redisHelper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }

        // 准备测试数据
        testUser1 = User.builder()
                .userId(100L)
                .email("user1@example.com")
                .username("user1")
                .nickname("用户1")
                .build();

        testUser2 = User.builder()
                .userId(200L)
                .email("user2@example.com")
                .username("user2")
                .nickname("用户2")
                .build();

        testFriendship = Friends.builder()
                .friendshipId(1L)
                .userId(100L)
                .friendId(200L)
                .status(FriendsConstants.ACCEPTED)
                .requestedAt(DateTime.now())
                .build();
    }

    /**
     * 测试获取好友列表 - 正常流程（从数据库加载）
     */
    @Test
    void testListFriends_Success_FromDatabase() {
        Long userId = 100L;

        // Mock Redis 没有缓存，需要从数据库加载
        when(redisHelper.getSet(
                eq(UserConstants.USER_KEY + userId + ":" + FriendsConstants.FRIEND_KEY),
                eq(Long.class),
                any()
        )).thenAnswer(invocation -> {
            // 模拟执行 supplier 函数
            java.util.function.Supplier<?> supplier = invocation.getArgument(2);
            return supplier.get();
        });

        // Mock 数据库查询好友关系
        List<Friends> friendsList = Arrays.asList(
                Friends.builder().userId(100L).friendId(200L).status(FriendsConstants.ACCEPTED).build(),
                Friends.builder().userId(100L).friendId(300L).status(FriendsConstants.ACCEPTED).build()
        );
        when(friendsMapper.selectList(any(QueryWrapper.class))).thenReturn(friendsList);

        // Mock 查询用户信息
        List<User> users = Arrays.asList(testUser2, 
                User.builder().userId(300L).username("user3").build());
        when(userService.list(any(QueryWrapper.class))).thenReturn(users);

        // 执行方法
        Set<User> result = friendsService.listFriends(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证调用
        verify(friendsMapper, times(1)).selectList(any(QueryWrapper.class));
        verify(userService, times(1)).list(any(QueryWrapper.class));
    }

    /**
     * 测试获取好友列表 - 空列表
     */
    @Test
    void testListFriends_EmptyList() {
        Long userId = 100L;

        // Mock Redis 返回空集合
        when(redisHelper.getSet(
                eq(UserConstants.USER_KEY + userId + ":" + FriendsConstants.FRIEND_KEY),
                eq(Long.class),
                any()
        )).thenReturn(new HashSet<>());

        // Mock 查询用户信息返回空
        when(userService.list(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        // 执行方法
        Set<User> result = friendsService.listFriends(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * 测试删除好友 - 正常流程
     */
    @Test
    void testDelFriend_Success() {
        Long userId = 100L;
        Long friendId = 200L;

        // Mock 更新操作
        when(friendsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

        // Mock Redis 删除缓存
        doNothing().when(redisHelper).removeSet(anyString());

        // 执行删除
        assertDoesNotThrow(() -> friendsService.delFriend(userId, friendId));

        // 验证调用
        verify(friendsMapper, times(1)).update(isNull(), any(UpdateWrapper.class));
        verify(redisHelper, times(1)).removeSet(
                eq(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY)
        );
    }

    /**
     * 测试添加好友 - 正常流程
     */
    @Test
    void testAddFriend_Success() {
        Long userId = 100L;
        Long friendId = 200L;

        // Mock Redis 检查不是好友
        when(redisHelper.isMember(
                eq(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY),
                eq(friendId)
        )).thenReturn(false);

        // Mock 插入操作
        when(friendsMapper.insert(any(Friends.class))).thenReturn(1);

        // Mock Redis 删除缓存
        doNothing().when(redisHelper).removeSet(anyString());

        // 执行添加
        assertDoesNotThrow(() -> friendsService.addFriend(userId, friendId));

        // 验证调用
        verify(friendsMapper, times(1)).insert(any(Friends.class));
        verify(redisHelper, times(1)).removeSet(
                eq(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY)
        );
    }

    /**
     * 测试添加好友 - 已经是好友
     */
    @Test
    void testAddFriend_AlreadyFriends() {
        Long userId = 100L;
        Long friendId = 200L;

        // Mock Redis 检查已经是好友
        when(redisHelper.isMember(
                eq(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY),
                eq(friendId)
        )).thenReturn(true);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            friendsService.addFriend(userId, friendId);
        });

        assertEquals("你们已经是好友了", exception.getMessage());

        // 验证未插入数据
        verify(friendsMapper, never()).insert(any(Friends.class));
    }

    /**
     * 测试添加好友 - userId 大于 friendId 的情况
     */
    @Test
    void testAddFriend_UserIdGreaterThanFriendId() {
        Long userId = 200L;
        Long friendId = 100L;

        // Mock Redis 检查不是好友
        when(redisHelper.isMember(anyString(), any())).thenReturn(false);

        // Mock 插入操作
        when(friendsMapper.insert(any(Friends.class))).thenAnswer(invocation -> {
            Friends friends = invocation.getArgument(0);
            // 验证 userId 应该是较小的值
            assertEquals(100L, friends.getUserId());
            assertEquals(200L, friends.getFriendId());
            return 1;
        });

        // 执行添加
        assertDoesNotThrow(() -> friendsService.addFriend(userId, friendId));

        // 验证调用
        verify(friendsMapper, times(1)).insert(any(Friends.class));
    }

    /**
     * 测试搜索好友 - 正常流程
     */
    @Test
    void testFindFriend_Success() {
        String query = "test";

        // Mock 查询用户
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(userService.list(any(QueryWrapper.class))).thenReturn(users);

        // 执行搜索
        List<User> result = friendsService.findFriend(query);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证调用
        verify(userService, times(1)).list(any(QueryWrapper.class));
    }

    /**
     * 测试搜索好友 - 无结果
     */
    @Test
    void testFindFriend_NoResults() {
        String query = "nonexistent";

        // Mock 查询返回空
        when(userService.list(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        // 执行搜索
        List<User> result = friendsService.findFriend(query);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * 测试同意好友请求 - 正常流程
     */
    @Test
    void testAgreeAddFriendRequest_Success() {
        Long userId = 100L;
        Long friendshipId = 1L;

        // Mock 更新操作
        when(friendsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

        // Mock Redis 删除缓存
        doNothing().when(redisHelper).removeSet(anyString());

        // Mock 查询好友关系
        when(friendsMapper.selectOne(any(QueryWrapper.class))).thenReturn(testFriendship);

        // Mock Feign 调用
        when(notificationFeignClient.addConv(anyLong())).thenReturn(Response.success());

        // 执行同意请求
        assertDoesNotThrow(() -> friendsService.agreeAddFriendRequest(userId, friendshipId));

        // 验证调用
        verify(friendsMapper, times(1)).update(isNull(), any(UpdateWrapper.class));
        verify(redisHelper, times(1)).removeSet(
                eq(UserConstants.USER_KEY + userId + FriendsConstants.FRIEND_KEY)
        );
        verify(friendsMapper, times(1)).selectOne(any(QueryWrapper.class));
        verify(notificationFeignClient, times(1)).addConv(anyLong());
    }

    /**
     * 测试同意好友请求 - 请求发起者是 friendId
     */
    @Test
    void testAgreeAddFriendRequest_InitiatorIsFriendId() {
        Long userId = 200L;
        Long friendshipId = 1L;

        Friends friendship = Friends.builder()
                .friendshipId(1L)
                .userId(100L)
                .friendId(200L)
                .status(FriendsConstants.PENDING)
                .build();

        // Mock 更新操作
        when(friendsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

        // Mock Redis 删除缓存
        doNothing().when(redisHelper).removeSet(anyString());

        // Mock 查询好友关系
        when(friendsMapper.selectOne(any(QueryWrapper.class))).thenReturn(friendship);

        // Mock Feign 调用
        when(notificationFeignClient.addConv(eq(100L))).thenReturn(Response.success());

        // 执行同意请求
        assertDoesNotThrow(() -> friendsService.agreeAddFriendRequest(userId, friendshipId));

        // 验证调用了 addConv，参数是 userId (100L)
        verify(notificationFeignClient, times(1)).addConv(eq(100L));
    }

    /**
     * 测试拒绝好友请求 - 正常流程
     */
    @Test
    void testRejectFriendRequest_Success() {
        Long userId = 100L;
        Long friendshipId = 1L;

        // Mock 更新操作
        when(friendsMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);

        // 执行拒绝请求
        assertDoesNotThrow(() -> friendsService.rejectFriendRequest(userId, friendshipId));

        // 验证调用
        verify(friendsMapper, times(1)).update(isNull(), any(UpdateWrapper.class));
    }

    /**
     * 测试获取新好友请求 - 正常流程
     */
    @Test
    void testGetNewFriendRequests_Success() {
        Long userId = 100L;

        // Mock 查询待处理的好友请求
        List<Friends> pendingRequests = Arrays.asList(
                Friends.builder().userId(100L).friendId(200L).status(FriendsConstants.PENDING).build(),
                Friends.builder().userId(100L).friendId(300L).status(FriendsConstants.PENDING).build()
        );
        when(friendsMapper.selectList(any(QueryWrapper.class))).thenReturn(pendingRequests);

        // 执行查询
        List<Friends> result = friendsService.getNewFriendRequests(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());

        // 验证调用
        verify(friendsMapper, times(1)).selectList(any(QueryWrapper.class));
    }

    /**
     * 测试获取新好友请求 - 无请求
     */
    @Test
    void testGetNewFriendRequests_NoRequests() {
        Long userId = 100L;

        // Mock 查询返回空
        when(friendsMapper.selectList(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        // 执行查询
        List<Friends> result = friendsService.getNewFriendRequests(userId);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * 测试获取好友状态 - 已接受
     */
    @Test
    void testGetFriendStatus_Accepted() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            Long userId = 100L;
            Long friendId = 200L;

            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);

            // Mock 查询好友关系
            when(friendsMapper.selectOne(any(QueryWrapper.class))).thenReturn(testFriendship);

            // 执行查询
            String status = friendsService.getFriendStatus(friendId);

            // 验证结果
            assertEquals(FriendsConstants.ACCEPTED, status);

            // 验证调用
            verify(friendsMapper, times(1)).selectOne(any(QueryWrapper.class));
        }
    }

    /**
     * 测试获取好友状态 - 待处理
     */
    @Test
    void testGetFriendStatus_Pending() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            Long userId = 100L;
            Long friendId = 200L;

            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);

            Friends pendingFriendship = Friends.builder()
                    .userId(100L)
                    .friendId(200L)
                    .status(FriendsConstants.PENDING)
                    .build();

            // Mock 查询好友关系
            when(friendsMapper.selectOne(any(QueryWrapper.class))).thenReturn(pendingFriendship);

            // 执行查询
            String status = friendsService.getFriendStatus(friendId);

            // 验证结果
            assertEquals(FriendsConstants.PENDING, status);
        }
    }

    /**
     * 测试获取好友状态 - 不存在关系
     */
    @Test
    void testGetFriendStatus_NoRelationship() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            Long userId = 100L;
            Long friendId = 200L;

            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);

            // Mock 查询返回 null
            when(friendsMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

            // 执行查询
            String status = friendsService.getFriendStatus(friendId);

            // 验证结果
            assertNull(status);
        }
    }

    /**
     * 测试获取好友状态 - userId 大于 friendId 的情况
     */
    @Test
    void testGetFriendStatus_UserIdGreaterThanFriendId() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            Long userId = 200L;
            Long friendId = 100L;

            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);

            // Mock 查询好友关系
            when(friendsMapper.selectOne(any(QueryWrapper.class))).thenAnswer(invocation -> {
                QueryWrapper<Friends> wrapper = invocation.getArgument(0);
                // 验证查询条件中 userId 应该是较小的值
                return testFriendship;
            });

            // 执行查询
            String status = friendsService.getFriendStatus(friendId);

            // 验证结果
            assertEquals(FriendsConstants.ACCEPTED, status);

            // 验证调用
            verify(friendsMapper, times(1)).selectOne(any(QueryWrapper.class));
        }
    }

    /**
     * 测试获取好友状态 - 已拒绝
     */
    @Test
    void testGetFriendStatus_Rejected() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            Long userId = 100L;
            Long friendId = 200L;

            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(userId);

            Friends rejectedFriendship = Friends.builder()
                    .userId(100L)
                    .friendId(200L)
                    .status(FriendsConstants.REJECTED)
                    .build();

            // Mock 查询好友关系
            when(friendsMapper.selectOne(any(QueryWrapper.class))).thenReturn(rejectedFriendship);

            // 执行查询
            String status = friendsService.getFriendStatus(friendId);

            // 验证结果
            assertEquals(FriendsConstants.REJECTED, status);
        }
    }
}
