package org.millburn.kiosk.websocket;

import org.millburn.kiosk.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(Server.getCurrent().setSocketHandler(new SocketHandler()), "/socket").setAllowedOrigins("*").withSockJS();
    }
}