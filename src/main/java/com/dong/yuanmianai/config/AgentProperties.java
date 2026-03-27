package com.dong.yuanmianai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 服务配置
 */
@Configuration
@ConfigurationProperties(prefix = "agent")
@Data
public class AgentProperties {

    /**
     * Agent 服务地址
     */
    private String baseUrl = "http://localhost:8291";

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeoutMs = 3000;

    /**
     * 响应超时时间（毫秒）
     */
    private Integer readTimeoutMs = 120000;
}
