package com.dong.yuanmianai.ai.truncator;

import org.springframework.stereotype.Component;

/**
 * Prompt 截断器，避免超长上下文
 */
@Component
public class PromptTruncator {

    public String truncate(String prompt, int maxLength) {
        if (prompt == null) {
            return "";
        }
        if (prompt.length() <= maxLength) {
            return prompt;
        }
        return prompt.substring(0, maxLength);
    }
}
