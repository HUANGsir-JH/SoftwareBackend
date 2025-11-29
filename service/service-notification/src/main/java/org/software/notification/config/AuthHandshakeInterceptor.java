package org.software.notification.config;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * 在握手阶段（HTTP协议）验证SaToken并提取用户信息
 */
@Component
@Slf4j
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        if (request instanceof ServletServerHttpRequest) {

            try {
                // 在握手阶段（还是HTTP请求），SaToken可以正常从Header中获取token
                // 验证token并获取用户ID
                long userId = StpUtil.getLoginIdAsLong();
                
                // 将用户ID存入WebSocketSession的attributes中，供后续使用
                attributes.put("userId", userId);
                
                log.info("WebSocket握手成功，用户ID: {}", userId);
                return true; // 允许握手
                
            } catch (Exception e) {
                // Token验证失败
                log.error("WebSocket握手失败，token验证失败: {}", e.getMessage());
                return false; // 拒绝握手
            }
        }
        
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后的回调，可以记录日志等
        if (exception != null) {
            log.error("WebSocket握手异常", exception);
        }
    }
}
