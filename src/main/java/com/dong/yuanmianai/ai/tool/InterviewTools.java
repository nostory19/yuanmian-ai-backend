package com.dong.yuanmianai.ai.tool;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * 可供 Agent 调用的工具函数
 */
@Component
public class InterviewTools {

    @Tool("计算两个整数的差值绝对值")
    public int absDiff(int a, int b) {
        return Math.abs(a - b);
    }

    @Tool("将文本压缩为简短摘要")
    public String shortSummary(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }
}
