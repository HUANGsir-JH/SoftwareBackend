package org.software.user.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import org.software.model.Response;
import org.software.model.exception.BusinessException;
import org.software.model.user.EmailLoginRequest;
import org.software.model.user.RegisterRequest;
import org.software.model.user.UsernameLoginRequest;
import org.software.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 登录控制器
 * 处理用户登录、注册、登出相关接口
 */
@RestController
@RequestMapping("/user")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 邮箱登录
     * 登录接口,根据邮箱和密码进行登录。登陆后要把用户id保存到session当中
     * 
     * @param loginRequest 包含email和password
     * @return 返回token和userId
     */
    @PostMapping("/login/email")
    public Response loginByEmail(@RequestBody EmailLoginRequest loginRequest) throws BusinessException {
        SaTokenInfo token = userService.validateEmailLogin(loginRequest);
        return Response.success(Map.of("userId", token.getLoginId(), "token", token.getTokenValue()));
    }

    /**
     * 用户名登录
     * 登录接口,根据用户名和密码进行。除了邮箱登陆外的第二个选择,可选是否要做
     * 
     * @param loginRequest 包含username和password
     * @return 返回token和id
     */
    @PostMapping("/login/username")
    public Response loginByUsername(@RequestBody UsernameLoginRequest loginRequest) throws BusinessException {
        SaTokenInfo token = userService.validateUsernameLogin(loginRequest);
        return Response.success(Map.of("userId", token.getLoginId(), "token", token.getTokenValue()));
    }

    /**
     * 用户注册
     * 用户注册接口,email、username、password、confirmPassword
     * - 邮箱查重
     * - 密码加密
     * - 检验两次密码是否相同(前后端都要校验)
     * 
     * @param registerRequest 包含email、username、password、confirmPassword
     * @return 注册结果
     */
    @PostMapping("/register")
    public Response register(@RequestBody RegisterRequest registerRequest) throws BusinessException {
        userService.register(registerRequest);
        return Response.success();
    }

    /**
     * 用户登出
     * 用户登出接口
     * - 服务端在session获取用户id,logout即可
     * 
     * @return 登出结果
     */
    @PutMapping("/logout")
    public Response logout() {
        StpUtil.logout();
        return Response.success();
    }

}
