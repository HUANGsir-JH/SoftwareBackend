package org.software.user.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.media.UploadD;
import org.software.model.exception.SystemException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.user.*;

import java.util.List;


/**
 * 用户表(User)表服务接口
 *
 * @author Ra1nbot
 * @since 2025-11-09 00:11:20
 */
public interface UserService extends IService<User> {

    SaTokenInfo validateEmailLogin(EmailLoginRequest loginRequest) throws SystemException;

    SaTokenInfo validateUsernameLogin(UsernameLoginRequest loginRequest) throws SystemException;

    void updatePassword(PasswordView passV) throws SystemException;

    void register(RegisterRequest registerRequest) throws SystemException;

    PageResult bPage(PageQuery pageQuery, PageUserD pageUserD);

    String updateAvatar(UploadD uploadD) throws SystemException;

    String updateBG(UploadD uploadD) throws SystemException;
}

