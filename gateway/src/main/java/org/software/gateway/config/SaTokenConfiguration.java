package org.software.gateway.config;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.exception.BusinessException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.software.model.Response;
import org.software.model.constants.UserConstants;

import java.util.Objects;

@Slf4j
@Configuration
public class SaTokenConfiguration {

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
            // 拦截所有路径
            .addInclude("/**")
            // 放行无需登录的路径（注意：这些路径不会进入 setAuth 逻辑）
            .addExclude(
                    "/user/login/**", "/user/register/**", "/user/forget/**",
                    "/content/all", "/content/view"
            )
            // 开启登录认证
            .setAuth(auth -> {
                try {
                    SaRouter.match("/**", r -> StpUtil.checkLogin());
                }catch (Exception e) {
                    log.error("未登录用户尝试访问需要登录的接口");
                    throw new BusinessException(HttpCodeEnum.NEED_LOGIN);
                }

                // 管理员接口权限控制
                SaRouter.match("**/b/**", r -> {
                    Long userId = StpUtil.getLoginIdAsLong();
                    if (!Objects.equals(userId, UserConstants.ADMIN_USER_ID)) {
                        log.error("非管理员用户尝试访问管理员接口 | userId = {}", userId);
                        throw new BusinessException(HttpCodeEnum.NO_OPERATOR_AUTH);
                    }
                });
            })
            // 异常处理
            .setError(e -> {
                log.error("Sa-Token 鉴权异常", e);
                BusinessException be;
                if (e instanceof BusinessException) {
                    be = (BusinessException) e;
                } else {
                    be = new BusinessException(HttpCodeEnum.SYSTEM_ERROR);
                }
                Response response = Response.error(be.getCode(), be.getMessage());
                return JSON.toJSONString(response, JSONWriter.Feature.WriteMapNullValue);
            });
    }
}