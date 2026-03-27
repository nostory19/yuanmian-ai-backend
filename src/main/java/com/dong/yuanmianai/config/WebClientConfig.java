package com.dong.yuanmianai.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

/**
 * WebClient 配置
 */
@Configuration
public class WebClientConfig {

    @Resource
    private AgentProperties agentProperties;

    @Bean("agentWebClient")
    public WebClient agentWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, agentProperties.getConnectTimeoutMs())
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(agentProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)));
        return WebClient.builder()
                .baseUrl(agentProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
