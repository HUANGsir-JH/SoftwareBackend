package org.software.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.model.user.User;
import org.software.user.mapper.UserMapper;
import org.software.user.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户表(User)表服务实现类
 *
 * @author Ra1nbot
 * @since 2025-11-09 00:11:21
 */
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}

