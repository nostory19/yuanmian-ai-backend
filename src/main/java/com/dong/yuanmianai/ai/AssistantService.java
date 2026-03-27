package com.dong.yuanmianai.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 通用助手 Agent
 */
public interface AssistantService {

    @SystemMessage("你是刷题与面试领域的智能助手，回答要准确、简洁、结构化。")
    String chat(@UserMessage String userMessage);
}
