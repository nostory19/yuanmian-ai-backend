package com.dong.yuanmianai.ai.guardrail;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 基础护栏：长度限制 + 敏感词过滤占位
 */
@Component
public class InterviewGuardrail {

    private static final int MAX_INPUT_LENGTH = 4000;

    public String sanitize(String input) {
        String safe = StringUtils.defaultString(input);
        if (safe.length() > MAX_INPUT_LENGTH) {
            safe = safe.substring(0, MAX_INPUT_LENGTH);
        }
        return safe.replace("忽略之前的指令", "【已过滤】");
    }
}
