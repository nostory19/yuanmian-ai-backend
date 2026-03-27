package com.dong.yuanmianai.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 报告 Agent
 */
public interface ReportAgentService {

    @SystemMessage("你是面试报告专家，请基于面试过程输出能力画像、优势短板与学习建议。")
    String buildReport(@UserMessage String reportPrompt);
}
