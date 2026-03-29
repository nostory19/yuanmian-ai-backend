package com.dong.yuanmianai.service;

import com.dong.yuanmianai.model.dto.assistant.AiAssistantChatRequest;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantChatVO;
import reactor.core.publisher.Flux;

/**
 * Agent 代理服务
 */
public interface AgentProxyService {

    /**
     * 代理 Agent 流式对话
     */
    Flux<String> proxyChatStream(AiAssistantChatRequest request);

    /**
     * 代理 Agent 非流式对话
     */
    AiAssistantChatVO proxyChat(AiAssistantChatRequest request);

    /**
     * 悬浮助手：直连 LLM（/assistant），与面试 LangGraph（/agent）隔离
     */
    Flux<String> proxyFloatingAssistantStream(AiAssistantChatRequest request);

    /**
     * 悬浮助手非流式
     */
    AiAssistantChatVO proxyFloatingAssistantChat(AiAssistantChatRequest request);
}
