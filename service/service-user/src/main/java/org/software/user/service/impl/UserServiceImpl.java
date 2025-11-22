package org.software.user.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.software.feign.MediaFeignClient;
import org.software.model.Response;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.constants.UserConstants;
import org.software.model.content.media.UploadD;
import org.software.model.content.media.UploadV;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.user.*;
import org.software.user.mapper.UserMapper;
import org.software.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 用户表(User)表服务实现类
 *
 * @author Ra1nbot
 * @since 2025-11-09 00:11:21
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MediaFeignClient mediaFeignClient;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SaTokenInfo validateEmailLogin(EmailLoginRequest loginRequest) throws BusinessException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", loginRequest.getEmail())
                .eq("is_active", UserConstants.USER_ACTIVE);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            log.warn("{} | email: {}", HttpCodeEnum.LOGIN_ERROR.getMsg(), loginRequest.getEmail());
            throw new BusinessException(HttpCodeEnum.NEED_LOGIN);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            log.warn("{} | email: {}", HttpCodeEnum.LOGIN_ERROR.getMsg(), loginRequest.getEmail());
            throw new BusinessException(HttpCodeEnum.LOGIN_ERROR);
        }

        StpUtil.login(user.getUserId());
        log.info("用户登录 | user: {}", user.getUserId());
        return StpUtil.getTokenInfo();

    }

    @Override
    public SaTokenInfo validateUsernameLogin(UsernameLoginRequest loginRequest) throws BusinessException {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginRequest.getUsername())
                .eq("is_active", UserConstants.USER_ACTIVE);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            log.warn("{} | username: {}", HttpCodeEnum.LOGIN_ERROR.getMsg(), loginRequest.getUsername());
            throw new BusinessException(HttpCodeEnum.NEED_LOGIN);
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            log.warn(HttpCodeEnum.LOGIN_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.LOGIN_ERROR);
        }
        StpUtil.login(user.getUserId());
        log.info("用户登录: {}", user.getUserId());
        return StpUtil.getTokenInfo();
    }

    @Override
    public void updatePassword(PasswordView passV) throws BusinessException {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(passV.getOldPassword(), user.getPassword())){
            log.warn(HttpCodeEnum.LOGIN_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.LOGIN_ERROR);
        }

        if (Objects.equals(passV.getNewPassword(), passV.getOldPassword())) {
            log.warn(HttpCodeEnum.OLD_AND_NEW_PASSWORD_SAME.getMsg());
            throw new BusinessException(HttpCodeEnum.OLD_AND_NEW_PASSWORD_SAME);
        }

        if (!Objects.equals(passV.getNewPassword(), passV.getConfirmPassword())) {
            log.warn(HttpCodeEnum.PASSWORD_NOT_MATCH.getMsg());
            throw new BusinessException(HttpCodeEnum.PASSWORD_NOT_MATCH);
        }

        user.setPassword(passwordEncoder.encode(passV.getNewPassword()));
        updateById(user);
        log.info("{} | userId: {}", HttpCodeEnum.SUCCESS.getMsg(), userId);

        StpUtil.logout();
    }

    @Override
    public String updateAvatar(UploadD uploadD) throws BusinessException {
        Response response = mediaFeignClient.upload(uploadD);
        if (response.getCode() != HttpCodeEnum.SUCCESS.getCode() || response.getData() == null) {
            log.warn("{} | uploadD: {}", HttpCodeEnum.UPLOAD_PRESIGNED_URL_FAILED.getMsg(), JSON.toJSONString(uploadD));
            throw new BusinessException(HttpCodeEnum.UPLOAD_PRESIGNED_URL_FAILED);
        }

        String json = JSON.toJSONString(response.getData());
        List<UploadV> list = JSON.parseObject(json, new TypeReference<>() {});

        Long userId = StpUtil.getLoginIdAsLong();
        User user = User.builder()
                .userId(userId)
                .avatar(list.get(0).getObjectKey())
                .build();
        updateById(user);
        log.info("{} | userId: {} | avatar: {}", HttpCodeEnum.SUCCESS.getMsg(), userId, list.get(0).getObjectKey());
        return list.get(0).getObjectKey();
    }

    @Override
    public String updateBG(UploadD uploadD) throws BusinessException {
        Response response = mediaFeignClient.upload(uploadD);
        if (response.getCode() != HttpCodeEnum.SUCCESS.getCode() || response.getData() == null) {
            log.warn("{} | uploadD: {}", HttpCodeEnum.UPLOAD_PRESIGNED_URL_FAILED.getMsg(), JSON.toJSONString(uploadD));
            throw new BusinessException(HttpCodeEnum.UPLOAD_PRESIGNED_URL_FAILED);
        }

        String json = JSON.toJSONString(response.getData());
        List<UploadV> list = JSON.parseObject(json, new TypeReference<>() {});

        Long userId = StpUtil.getLoginIdAsLong();
        User user = User.builder()
                .userId(userId)
                .backgroundImage(list.get(0).getObjectKey())
                .build();
        updateById(user);
        log.info("{} | userId: {} | backgroundImage: {}", HttpCodeEnum.SUCCESS.getMsg(), userId, list.get(0).getObjectKey());
        return list.get(0).getObjectKey();
    }

    @Override
    public void register(RegisterRequest registerRequest) throws BusinessException {
        // 1. 验证邮箱和用户名是否已被注册
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", registerRequest.getEmail())
                    .or()
                    .eq("username", registerRequest.getUsername());

        Long cnt = userMapper.selectCount(queryWrapper);
        if (cnt > 1){
            log.warn("{} | email: {} | username: {}", HttpCodeEnum.REGISTERED.getMsg(), registerRequest.getEmail(), registerRequest.getUsername());
            throw new BusinessException(HttpCodeEnum.REGISTERED);
        }

        // 2. 验证两次密码是否相同
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            log.warn("{} | email: {}", HttpCodeEnum.PASSWORD_NOT_MATCH.getMsg(), registerRequest.getEmail());
            throw new BusinessException(HttpCodeEnum.PASSWORD_NOT_MATCH);
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
        log.info("{} | userId: {} | email: {}", HttpCodeEnum.SUCCESS.getMsg(), user.getUserId(), user.getEmail());
    }

    // ==================================== B端 =================================================
    @Override
    public PageResult bPage(PageQuery pageQuery, PageUserD pageUserD) {
        log.info("用户列表查询 | pageNum: {} pageSize: {} filters: {}", pageQuery.getPageNum(), pageQuery.getPageSize(), JSON.toJSONString(pageUserD));
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

