package org.software.user.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.common.util.RedisHelper;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.constants.UserConstants;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.user.*;
import org.software.user.mapper.UserMapper;
import org.software.user.util.EmailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 白盒测试
 * 测试重点：核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private EmailSender emailSender;
    private RedisHelper redisHelper;
    private UserServiceImpl userService;

    private User testUser;
    private EmailLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        userMapper = mock(UserMapper.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailSender = mock(EmailSender.class);
        redisHelper = mock(RedisHelper.class);

        // 手动创建 UserServiceImpl 并注入依赖
        userService = new UserServiceImpl() {
            @Override
            public boolean updateById(User entity) {
                int result = userMapper.updateById(entity);
                return result > 0;
            }

            @Override
            public boolean removeById(java.io.Serializable id) {
                int result = userMapper.deleteById(id);
                return result > 0;
            }

            public boolean update(User entity, UpdateWrapper<User> updateWrapper) {
                int result = userMapper.update(entity, updateWrapper);
                return result > 0;
            }
        };

        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field userMapperField = UserServiceImpl.class.getDeclaredField("userMapper");
            userMapperField.setAccessible(true);
            userMapperField.set(userService, userMapper);

            java.lang.reflect.Field passwordEncoderField = UserServiceImpl.class.getDeclaredField("passwordEncoder");
            passwordEncoderField.setAccessible(true);
            passwordEncoderField.set(userService, passwordEncoder);

            java.lang.reflect.Field emailSenderField = UserServiceImpl.class.getDeclaredField("emailSender");
            emailSenderField.setAccessible(true);
            emailSenderField.set(userService, emailSender);

            java.lang.reflect.Field redisHelperField = UserServiceImpl.class.getDeclaredField("redisHelper");
            redisHelperField.setAccessible(true);
            redisHelperField.set(userService, redisHelper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }

        // 准备测试数据
        testUser = User.builder()
                .userId(100L)
                .email("test@example.com")
                .username("testuser")
                .password("$2a$10$encodedPassword")
                .isActive(UserConstants.USER_ACTIVE)
                .build();

        loginRequest = new EmailLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    /**
     * 测试邮箱登录 - 正常流程
     */
    @Test
    void testValidateEmailLogin_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            // Mock 用户查询
            when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);

            // Mock 密码验证
            when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

            // Mock SaToken
            SaTokenInfo tokenInfo = new SaTokenInfo();
            tokenInfo.setTokenValue("mock-token");
            stpUtilMock.when(() -> StpUtil.login(100L)).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getTokenInfo).thenReturn(tokenInfo);

            // 执行登录
            SaTokenInfo result = userService.validateEmailLogin(loginRequest);

            // 验证结果
            assertNotNull(result);
            assertEquals("mock-token", result.getTokenValue());

            // 验证调用
            verify(userMapper, times(1)).selectOne(any(QueryWrapper.class));
            verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
        }
    }

    /**
     * 测试邮箱登录 - 用户不存在
     */
    @Test
    void testValidateEmailLogin_UserNotExist() {
        // Mock 用户查询返回null
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.validateEmailLogin(loginRequest);
        });

        assertEquals(HttpCodeEnum.USER_NOT_EXIST.getCode(), exception.getCode());
    }

    /**
     * 测试邮箱登录 - 密码错误
     */
    @Test
    void testValidateEmailLogin_WrongPassword() {
        // Mock 用户查询
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);

        // Mock 密码验证失败
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(false);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.validateEmailLogin(loginRequest);
        });

        assertEquals(HttpCodeEnum.LOGIN_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试用户注册 - 正常流程
     */
    @Test
    void testRegister_Success() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setNickname("新用户");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        // Mock 邮箱未注册
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        // Mock 密码加密
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedNewPassword");

        // Mock 插入用户
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(101L);
            return 1;
        });

        // 执行注册
        assertDoesNotThrow(() -> userService.register(registerRequest));

        // 验证调用
        verify(userMapper, times(1)).selectCount(any(QueryWrapper.class));
        verify(userMapper, times(1)).insert(any(User.class));
    }

    /**
     * 测试用户注册 - 邮箱已注册
     */
    @Test
    void testRegister_EmailAlreadyRegistered() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");

        // Mock 邮箱已注册
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals(HttpCodeEnum.REGISTERED.getCode(), exception.getCode());
    }

    /**
     * 测试用户注册 - 两次密码不一致
     */
    @Test
    void testRegister_PasswordNotMatch() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password456");

        // Mock 邮箱未注册
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals(HttpCodeEnum.PASSWORD_NOT_MATCH.getCode(), exception.getCode());
    }

    /**
     * 测试修改密码 - 正常流程
     */
    @Test
    void testUpdatePassword_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            PasswordView passV = new PasswordView();
            passV.setOldPassword("oldPassword");
            passV.setNewPassword("newPassword");
            passV.setConfirmPassword("newPassword");

            // Mock 查询用户
            when(userMapper.selectById(100L)).thenReturn(testUser);

            // Mock 密码验证
            when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$encodedNewPassword");

            // Mock 更新
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            // 执行修改密码
            assertDoesNotThrow(() -> userService.updatePassword(passV));

            // 验证调用
            verify(userMapper, times(1)).selectById(100L);
            verify(userMapper, times(1)).updateById(any(User.class));
        }
    }

    /**
     * 测试修改密码 - 旧密码错误
     */
    @Test
    void testUpdatePassword_WrongOldPassword() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            PasswordView passV = new PasswordView();
            passV.setOldPassword("wrongPassword");
            passV.setNewPassword("newPassword");
            passV.setConfirmPassword("newPassword");

            // Mock 查询用户
            when(userMapper.selectById(100L)).thenReturn(testUser);

            // Mock 密码验证失败
            when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updatePassword(passV);
            });

            assertEquals(HttpCodeEnum.LOGIN_ERROR.getCode(), exception.getCode());
        }
    }

    /**
     * 测试修改密码 - 新旧密码相同
     */
    @Test
    void testUpdatePassword_SameOldAndNewPassword() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            PasswordView passV = new PasswordView();
            passV.setOldPassword("samePassword");
            passV.setNewPassword("samePassword");
            passV.setConfirmPassword("samePassword");

            // Mock 查询用户
            when(userMapper.selectById(100L)).thenReturn(testUser);

            // Mock 密码验证通过
            when(passwordEncoder.matches("samePassword", testUser.getPassword())).thenReturn(true);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updatePassword(passV);
            });

            assertEquals(HttpCodeEnum.OLD_AND_NEW_PASSWORD_SAME.getCode(), exception.getCode());
        }
    }

    /**
     * 测试修改密码 - 新密码和确认密码不一致
     */
    @Test
    void testUpdatePassword_NewPasswordNotMatch() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            PasswordView passV = new PasswordView();
            passV.setOldPassword("oldPassword");
            passV.setNewPassword("newPassword1");
            passV.setConfirmPassword("newPassword2");

            // Mock 查询用户
            when(userMapper.selectById(100L)).thenReturn(testUser);

            // Mock 密码验证通过
            when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                userService.updatePassword(passV);
            });

            assertEquals(HttpCodeEnum.PASSWORD_NOT_MATCH.getCode(), exception.getCode());
        }
    }

    /**
     * 测试分页查询 - 带条件过滤
     */
    @Test
    void testBPage_WithFilters() {
        PageQuery pageQuery = new PageQuery(1, 10);
        PageUserD pageUserD = new PageUserD();
        pageUserD.setUsername("test");
        pageUserD.setEmail("test@example.com");
        pageUserD.setIsActive(1);

        // Mock 分页查询
        Page<User> page = new Page<>(1, 10);
        List<User> users = new ArrayList<>();
        users.add(testUser);
        page.setRecords(users);
        page.setTotal(1);

        when(userMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // 执行查询
        PageResult result = userService.bPage(pageQuery, pageUserD);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());

        // 验证调用
        verify(userMapper, times(1)).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    /**
     * 测试分页查询 - 无条件过滤
     */
    @Test
    void testBPage_NoFilters() {
        PageQuery pageQuery = new PageQuery(1, 10);
        PageUserD pageUserD = new PageUserD();

        // Mock 分页查询
        Page<User> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(userMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        // 执行查询
        PageResult result = userService.bPage(pageQuery, pageUserD);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    /**
     * 测试删除用户
     */
    @Test
    void testDeleteUser() {
        Long userId = 100L;

        // Mock 删除操作
        when(userMapper.deleteById(userId)).thenReturn(1);

        // 执行删除
        assertDoesNotThrow(() -> userService.deleteUser(userId));

        // 验证调用
        verify(userMapper, times(1)).deleteById(userId);
    }

    /**
     * 测试忘记密码 - 发送验证码
     */
    @Test
    void testForgetPassword_Success() {
        try (MockedStatic<RandomUtil> randomUtilMock = mockStatic(RandomUtil.class)) {
            String email = "test@example.com";

            // Mock Redis没有缓存
            when(redisHelper.hasKey(email)).thenReturn(false);

            // Mock 生成验证码
            randomUtilMock.when(() -> RandomUtil.randomNumbers(6)).thenReturn("123456");

            // Mock Redis存储
            doNothing().when(redisHelper).addValue(
                    eq(UserConstants.FORGET_CODE_KEY + email),
                    eq("123456"),
                    eq(5 * 60),
                    eq(TimeUnit.SECONDS)
            );

            // Mock 发送邮件
            doNothing().when(emailSender).sendVerificationCodeEmail(email, "123456");

            // 执行发送验证码
            assertDoesNotThrow(() -> userService.forgetPassword(email));

            // 验证调用
            verify(redisHelper, times(1)).hasKey(email);
            verify(redisHelper, times(1)).addValue(
                    eq(UserConstants.FORGET_CODE_KEY + email),
                    eq("123456"),
                    eq(5 * 60),
                    eq(TimeUnit.SECONDS)
            );
            verify(emailSender, times(1)).sendVerificationCodeEmail(email, "123456");
        }
    }

    /**
     * 测试忘记密码 - 验证码已存在
     */
    @Test
    void testForgetPassword_CodeAlreadySent() {
        String email = "test@example.com";

        // Mock Redis已有缓存
        when(redisHelper.hasKey(email)).thenReturn(true);

        // 验证异常
        assertThrows(BusinessException.class, () -> {
            userService.forgetPassword(email);
        });

        // 验证未发送邮件
        verify(emailSender, never()).sendVerificationCodeEmail(anyString(), anyString());
    }

    /**
     * 测试验证验证码 - 正常流程
     */
    @Test
    void testVerifyCode_Success() {
        String email = "test@example.com";
        String code = "123456";

        // Mock Redis获取验证码
        when(redisHelper.getValue(UserConstants.FORGET_CODE_KEY + email)).thenReturn("123456");

        // Mock 存储token
        doNothing().when(redisHelper).addValue(
                startsWith(UserConstants.FORGET_TOKEN_KEY + email),
                anyString(),
                eq(3 * 60),
                eq(TimeUnit.SECONDS)
            );

        // 执行验证
        String token = userService.verifyCode(email, code);

        // 验证结果
        assertNotNull(token);

        // 验证调用
        verify(redisHelper, times(1)).getValue(UserConstants.FORGET_CODE_KEY + email);
        verify(redisHelper, times(1)).addValue(
                eq(UserConstants.FORGET_TOKEN_KEY + email),
                anyString(),
                eq(3 * 60),
                eq(TimeUnit.SECONDS)
        );
    }

    /**
     * 测试验证验证码 - 验证码错误
     */
    @Test
    void testVerifyCode_InvalidCode() {
        String email = "test@example.com";
        String code = "999999";

        // Mock Redis获取验证码
        when(redisHelper.getValue(UserConstants.FORGET_CODE_KEY + email)).thenReturn("123456");

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.verifyCode(email, code);
        });

        assertEquals(HttpCodeEnum.CODE_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试验证验证码 - 验证码不存在
     */
    @Test
    void testVerifyCode_CodeNotFound() {
        String email = "test@example.com";
        String code = "123456";

        // Mock Redis未找到验证码
        when(redisHelper.getValue(UserConstants.FORGET_CODE_KEY + email)).thenReturn(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.verifyCode(email, code);
        });

        assertEquals(HttpCodeEnum.CODE_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试忘记密码更新 - token无效
     */
    @Test
    void testForgetPasswordUpdate_InvalidToken() {
        PasswordView passV = new PasswordView();
        passV.setEmail("test@example.com");
        passV.setToken("invalid-token");
        passV.setNewPassword("newPassword");
        passV.setConfirmPassword("newPassword");

        // Mock Redis获取token
        when(redisHelper.getValue(UserConstants.FORGET_TOKEN_KEY + passV.getEmail()))
                .thenReturn("valid-token");

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.forgetPasswordUpdate(passV);
        });

        assertEquals(HttpCodeEnum.TOKEN_INVALID.getCode(), exception.getCode());
    }

    /**
     * 测试忘记密码更新 - 密码不一致
     */
    @Test
    void testForgetPasswordUpdate_PasswordNotMatch() {
        PasswordView passV = new PasswordView();
        passV.setEmail("test@example.com");
        passV.setToken("valid-token");
        passV.setNewPassword("newPassword1");
        passV.setConfirmPassword("newPassword2");

        // Mock Redis获取token
        when(redisHelper.getValue(UserConstants.FORGET_TOKEN_KEY + passV.getEmail()))
                .thenReturn("valid-token");

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.forgetPasswordUpdate(passV);
        });

        assertEquals(HttpCodeEnum.PASSWORD_NOT_MATCH.getCode(), exception.getCode());
    }
}
