package org.software.user.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.constants.UserConstants;
import org.software.model.exception.SystemException;
import org.software.model.user.EmailLoginRequest;
import org.software.model.user.RegisterRequest;
import org.software.model.user.User;
import org.software.model.user.UsernameLoginRequest;
import org.software.user.mapper.UserMapper;
import org.software.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户表(User)表服务实现类
 *
 * @author Ra1nbot
 * @since 2025-11-09 00:11:21
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SaTokenInfo validateEmailLogin(EmailLoginRequest loginRequest) throws SystemException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", loginRequest.getEmail())
                .eq("password", loginRequest.getPassword())
                .eq("is_active", UserConstants.USER_ACTIVE);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new SystemException(HttpCodeEnum.NEED_LOGIN);
        }
        StpUtil.login(user.getUserId());
        return StpUtil.getTokenInfo();

    }

    @Override
    public SaTokenInfo validateUsernameLogin(UsernameLoginRequest loginRequest) throws SystemException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginRequest.getUsername())
                .eq("password", loginRequest.getPassword())
                .eq("is_active", UserConstants.USER_ACTIVE);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new SystemException(HttpCodeEnum.NEED_LOGIN);
        }
        StpUtil.login(user.getUserId());
        return StpUtil.getTokenInfo();
    }

    @Override
    public void register(RegisterRequest registerRequest) throws SystemException {
        // 1. 验证邮箱是否已被注册
        Long cnt = userMapper.selectCount(new QueryWrapper<User>().eq("email", registerRequest.getEmail()));
        if (cnt > 1) {
            throw new SystemException(HttpCodeEnum.SYSTEM_ERROR.getCode(), "邮箱已被注册");
        }
        // 2. 验证两次密码是否相同
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new SystemException(HttpCodeEnum.SYSTEM_ERROR.getCode(), "两次输入的密码不一致");
        }
        // 3. 密码加密
        User user = User.builder()
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .isActive(UserConstants.USER_ACTIVE)
                .build();
        // 4. 保存用户信息
        userMapper.insert(user);
    }
}

