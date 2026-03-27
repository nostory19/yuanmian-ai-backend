package com.dong.yuanmianai.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 追问 Agent
 */
public interface FollowUpAgentService {

    @SystemMessage("你是技术面试追问官，请根据候选人回答薄弱点给出深入追问。")
    String generateFollowUpQuestion(@UserMessage String followUpPrompt);
}
