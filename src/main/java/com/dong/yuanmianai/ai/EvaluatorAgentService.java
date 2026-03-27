package com.dong.yuanmianai.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 评估 Agent
 */
public interface EvaluatorAgentService {

    @SystemMessage("你是技术面试评估官，请按正确性、表达能力、思路完整性评分，输出 JSON。")
    String evaluate(@UserMessage String evaluatePrompt);
}
