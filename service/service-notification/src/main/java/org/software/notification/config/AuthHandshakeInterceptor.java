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
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            // 1. 获取 URL 参数中的 userId (针对你提供的 ws://.../ws/chat?userId=1)
            String userIdStr = servletRequest.getServletRequest().getParameter("userId");
            
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    // 将字符串转为 Long
                    long userId = Long.parseLong(userIdStr);
                    
                    // 将用户ID存入 WebSocketSession 的 attributes 中
                    // 这样在 WebSocketHandler 的 session.getAttributes() 中就能拿到它
                    attributes.put("userId", userId);
                    
                    log.info("WebSocket 握手成功，通过参数获取到用户 ID: {}", userId);
                    return true;
                } catch (NumberFormatException e) {
                    log.error("WebSocket 握手失败，userId 格式不正确: {}", userIdStr);
                    return false;
                }
            }
            
            log.warn("WebSocket 握手失败，未能在参数中找到 userId");
            return false;
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
