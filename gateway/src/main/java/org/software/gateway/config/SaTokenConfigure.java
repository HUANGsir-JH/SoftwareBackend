package org.software.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.software.model.Response;
import org.software.model.constants.UserConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class SaTokenConfigure {
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude("/user/login/**", "/user/register")
                .setAuth(obj -> {
                    SaRouter
                            .match("/**", StpUtil::checkLogin)
                            .check(StpUtil::checkLogin);

                    SaRouter.match("/user/b", () -> {
                        Long userId = StpUtil.getLoginIdAsLong();
                        if (!Objects.equals(userId, UserConstants.ADMIN_USER_ID)) {
                            throw new RuntimeException("非管理员用户无法访问");
                        }
                    });
                })
                .setError(e -> {
                    return Response.error();
                });

    }
}
