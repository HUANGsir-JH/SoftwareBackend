package org.software.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.software.model.constants.HttpCodeEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.software.model.Response;
import org.software.model.constants.UserConstants;

import java.util.Objects;

@Configuration
public class SaTokenConfiguration {

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
            // 拦截所有路径
            .addInclude("/**")
            // 放行无需登录的路径（注意：这些路径不会进入 setAuth 逻辑）
            .addExclude("/user/login/**", "/user/register/**")
            // 开启登录认证
            .setAuth(auth -> {
                SaRouter.match("/**", r -> StpUtil.checkLogin());

                // 管理员接口权限控制
                SaRouter.match("/user/b/**", r -> {
                    Long userId = StpUtil.getLoginIdAsLong();
                    if (!Objects.equals(userId, UserConstants.ADMIN_USER_ID)) {
                        throw new RuntimeException("非管理员用户无法访问");
                    }
                });
            })
            // 异常处理
            .setError(e -> JSON.toJSONString(Response.error(HttpCodeEnum.SYSTEM_ERROR.getCode(), HttpCodeEnum.SYSTEM_ERROR.getMsg()), JSONWriter.Feature.PrettyFormat));
    }
}