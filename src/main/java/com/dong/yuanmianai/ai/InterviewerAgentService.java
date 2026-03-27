package com.dong.yuanmianai.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 出题 Agent
 */
public interface InterviewerAgentService {

    @SystemMessage("你是一名技术面试官，擅长根据岗位、难度和历史表现动态出题。")
    String generateQuestion(@UserMessage String contextPrompt);
}
