package org.software.user.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.feign.MediaFeignClient;
import org.software.model.Response;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.constants.UserConstants;
import org.software.model.content.media.UploadD;
import org.software.model.content.media.UploadV;
import org.software.model.exception.SystemException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.user.*;
import org.software.user.mapper.UserMapper;
import org.software.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
    private MediaFeignClient mediaFeignClient;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SaTokenInfo validateEmailLogin(EmailLoginRequest loginRequest) throws SystemException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", loginRequest.getEmail())
                .eq("is_active", UserConstants.USER_ACTIVE);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new SystemException(HttpCodeEnum.NEED_LOGIN);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new SystemException(HttpCodeEnum.LOGIN_ERROR);
        }
        StpUtil.login(user.getUserId());
        return StpUtil.getTokenInfo();

    }

    @Override
    public SaTokenInfo validateUsernameLogin(UsernameLoginRequest loginRequest) throws SystemException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginRequest.getUsername())
                .eq("is_active", UserConstants.USER_ACTIVE);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new SystemException(HttpCodeEnum.NEED_LOGIN);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new SystemException(HttpCodeEnum.LOGIN_ERROR);
        }
        StpUtil.login(user.getUserId());
        return StpUtil.getTokenInfo();
    }

    @Override
    public void updatePassword(PasswordView passV) throws SystemException {

        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(passV.getOldPassword(), user.getPassword())){
            throw new SystemException(HttpCodeEnum.SYSTEM_ERROR.getCode(), "旧密码错误");
        }

        if (Objects.equals(passV.getNewPassword(), passV.getOldPassword())) {
            throw new SystemException(HttpCodeEnum.SYSTEM_ERROR.getCode(), "新密码不能与旧密码相同");
        }

        if (!Objects.equals(passV.getNewPassword(), passV.getConfirmPassword())) {
            throw new SystemException(HttpCodeEnum.SYSTEM_ERROR.getCode(), "两次输入的密码不一致");
        }

        user.setPassword(passwordEncoder.encode(passV.getNewPassword()));
        updateById(user);

        StpUtil.logout();
    }

    @Override
    public String updateAvatar(UploadD uploadD) throws SystemException {
        Response response = mediaFeignClient.upload(uploadD);
        if (response.getCode() != HttpCodeEnum.SUCCESS.getCode() || response.getData() == null) {
            throw new SystemException(HttpCodeEnum.SYSTEM_ERROR.getCode(), "生成预签名url失败");
        }

        String json = JSON.toJSONString(response.getData());
        List<UploadV> list = JSON.parseObject(json, new TypeReference<>() {});

        Long userId = StpUtil.getLoginIdAsLong();
        User user = User.builder()
                .userId(userId)
                .avatar(list.get(0).getObjectKey())
                .build();
        updateById(user);
        return list.get(0).getObjectKey();
    }

    @Override
    public String updateBG(UploadD uploadD) throws SystemException {
        Response response = mediaFeignClient.upload(uploadD);
        if (response.getCode() != HttpCodeEnum.SUCCESS.getCode() || response.getData() == null) {
            throw new SystemException(HttpCodeEnum.SYSTEM_ERROR.getCode(), "生成预签名url失败");
        }

        List<UploadV> list = (List<UploadV>) response.getData();

        Long userId = StpUtil.getLoginIdAsLong();
        User user = User.builder()
                .userId(userId)
                .backgroundImage(list.get(0).getObjectKey())
                .build();
        updateById(user);
        return list.get(0).getObjectKey();
    }


    @Override
    public void register(RegisterRequest registerRequest) throws SystemException {
        // 1.1 验证邮箱是否已被注册
        Long cnt = userMapper.selectCount(new QueryWrapper<User>().eq("email", registerRequest.getEmail()));
        if (cnt > 1) {
            throw new SystemException(HttpCodeEnum.SYSTEM_ERROR.getCode(), "邮箱已被注册");
        }
        // 1.2 验证用户名是否已被注册
        cnt = userMapper.selectCount(new QueryWrapper<User>().eq("username", registerRequest.getUsername()));
        if (cnt > 1){
            throw new SystemException(HttpCodeEnum.SYSTEM_ERROR.getCode(), "用户名已被注册");
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

    // ==================================== B端 =================================================
    @Override
    public PageResult bPage(PageQuery pageQuery, PageUserD pageUserD) {
        Page<User> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (pageUserD.getUsername() != null && !pageUserD.getUsername().isEmpty()) {
            wrapper.like("username", pageUserD.getUsername());
        }
        if (pageUserD.getEmail() != null && !pageUserD.getEmail().isEmpty()) {
            wrapper.eq("email", pageUserD.getEmail());
        }
        if (pageUserD.getSex() != null && !pageUserD.getSex().isEmpty()) {
            wrapper.eq("sex", pageUserD.getSex());
        }
        if (pageUserD.getIsActive() != null) {
            wrapper.eq("is_active", pageUserD.getIsActive());
        }
        page = userMapper.selectPage(page, wrapper);

        return PageResult.builder()
                .total(page.getTotal())
                .pageNum(pageQuery.getPageNum())
                .pageSize(pageQuery.getPageSize())
                .records(page.getRecords())
                .build();
    }
}

