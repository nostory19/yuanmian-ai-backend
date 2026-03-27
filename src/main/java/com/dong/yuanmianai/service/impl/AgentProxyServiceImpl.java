package com.dong.yuanmianai.service.impl;

import com.dong.yuanmianai.model.dto.assistant.AiAssistantChatRequest;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantChatVO;
import com.dong.yuanmianai.service.AgentProxyService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Agent 代理服务实现
 */
@Service
public class AgentProxyServiceImpl implements AgentProxyService {

    @Resource(name = "agentWebClient")
    private WebClient agentWebClient;

    @Override
    public Flux<String> proxyChatStream(AiAssistantChatRequest request) {
        fillDefault(request);
        Map<String, Object> agentRequest = toAgentRequest(request);
        return agentWebClient.post()
                .uri("/agent/chat-stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(agentRequest)
                .retrieve()
                .bodyToFlux(String.class);
    }

    @Override
    public AiAssistantChatVO proxyChat(AiAssistantChatRequest request) {
        fillDefault(request);
        Map<String, Object> agentRequest = toAgentRequest(request);
        Map<String, Object> resp = agentWebClient.post()
                .uri("/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(agentRequest)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
        AiAssistantChatVO vo = new AiAssistantChatVO();
        if (resp == null) {
            vo.setSessionId(request.getSessionId());
            vo.setAnswer("Agent 服务暂无响应");
            vo.setSource("agent");
            return vo;
        }
        BeanUtils.copyProperties(resp, vo);
        Object agentSessionId = resp.get("session_id");
        if (agentSessionId != null) {
            vo.setSessionId(String.valueOf(agentSessionId));
        }
        Object answer = resp.get("answer");
        if (answer != null) {
            vo.setAnswer(String.valueOf(answer));
        }
        Object source = resp.get("source");
        if (source != null) {
            vo.setSource(String.valueOf(source));
        }
        if (StringUtils.isBlank(vo.getSessionId())) {
            vo.setSessionId(request.getSessionId());
        }
        if (StringUtils.isBlank(vo.getSource())) {
            vo.setSource("agent");
        }
        return vo;
    }

    private void fillDefault(AiAssistantChatRequest request) {
        if (request == null) {
            return;
        }
        if (StringUtils.isBlank(request.getSessionId())) {
            request.setSessionId("sess_" + UUID.randomUUID());
        }
        if (StringUtils.isBlank(request.getUserId())) {
            request.setUserId("anonymous");
        }
    }

    private Map<String, Object> toAgentRequest(AiAssistantChatRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("session_id", request.getSessionId());
        map.put("user_id", request.getUserId());
        map.put("message", request.getMessage());
        return map;
    }
}
