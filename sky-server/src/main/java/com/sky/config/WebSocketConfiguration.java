package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类，用于注册WebSocket的Bean
 */
@Configuration
public class WebSocketConfiguration {

    //负责扫描使用@ServerEndpoint注解的类并自动注册这些WebSocket端点。没有它，WebSocket端点将不会被识别，导致连接无法建立。
    //ServerEndpointExporter作为一个Bean被注册，可以确保在Spring应用启动时，所有的WebSocket端点都已被正确配置并可用。
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}
