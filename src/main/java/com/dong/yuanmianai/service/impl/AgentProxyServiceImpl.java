package com.dong.yuanmianai.service.impl;

import com.dong.yuanmianai.model.dto.assistant.AiAssistantChatRequest;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantChatVO;
import com.dong.yuanmianai.service.AgentProxyService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
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

    /**
     * 必须按 SSE 帧解码；{@code bodyToFlux(String.class)} 往往会整包缓冲后再发出，前端会一直转圈直到结束。
     */
    private static final ParameterizedTypeReference<ServerSentEvent<String>> SSE_STRING =
            new ParameterizedTypeReference<>() {
            };

    @Resource(name = "agentWebClient")
    private WebClient agentWebClient;

    @Override
    public Flux<String> proxyChatStream(AiAssistantChatRequest request) {
        return proxySseChatStream("/agent/chat-stream", request);
    }

    @Override
    public Flux<String> proxyFloatingAssistantStream(AiAssistantChatRequest request) {
        return proxySseChatStream("/assistant/chat-stream", request);
    }

    private Flux<String> proxySseChatStream(String uri, AiAssistantChatRequest request) {
        fillDefault(request);
        Map<String, Object> agentRequest = toAgentRequest(request);
        return agentWebClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(agentRequest)
                .retrieve()
                .bodyToFlux(SSE_STRING)
                .mapNotNull(ServerSentEvent::data);
    }

    @Override
    public AiAssistantChatVO proxyChat(AiAssistantChatRequest request) {
        return proxyJsonChat("/agent/chat", request);
    }

    @Override
    public AiAssistantChatVO proxyFloatingAssistantChat(AiAssistantChatRequest request) {
        return proxyJsonChat("/assistant/chat", request);
    }

    private AiAssistantChatVO proxyJsonChat(String uri, AiAssistantChatRequest request) {
        fillDefault(request);
        Map<String, Object> agentRequest = toAgentRequest(request);
        Map<String, Object> resp = agentWebClient.post()
                .uri(uri)
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
        Object nextAction = resp.get("next_action");
        if (nextAction != null) {
            vo.setNextAction(String.valueOf(nextAction));
        }
        Object score = resp.get("score");
        if (score instanceof Number number) {
            vo.setScore(number.intValue());
        }
        Object question = resp.get("question");
        if (question != null) {
            vo.setQuestion(String.valueOf(question));
        }
        Object weakness = resp.get("weakness");
        if (weakness != null) {
            vo.setWeakness(String.valueOf(weakness));
        }
        Object followUpQuestion = resp.get("follow_up_question");
        if (followUpQuestion != null) {
            vo.setFollowUpQuestion(String.valueOf(followUpQuestion));
        }
        Object report = resp.get("report");
        if (report != null) {
            vo.setReport(String.valueOf(report));
        }
        Object agentTrace = resp.get("agent_trace");
        if (agentTrace instanceof java.util.List<?> list) {
            java.util.List<String> traces = list.stream().map(String::valueOf).toList();
            vo.setAgentTrace(traces);
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
