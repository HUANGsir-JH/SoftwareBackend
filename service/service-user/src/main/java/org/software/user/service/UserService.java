package org.software.user.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.exception.SystemException;
import org.software.model.user.EmailLoginRequest;
import org.software.model.user.RegisterRequest;
import org.software.model.user.User;
import org.software.model.user.UsernameLoginRequest;


/**
 * 用户表(User)表服务接口
 *
 * @author Ra1nbot
 * @since 2025-11-09 00:11:20
 */
public interface UserService extends IService<User> {

    SaTokenInfo validateEmailLogin(EmailLoginRequest loginRequest) throws SystemException;

    SaTokenInfo validateUsernameLogin(UsernameLoginRequest loginRequest) throws SystemException;

    void register(RegisterRequest registerRequest) throws SystemException;
}

