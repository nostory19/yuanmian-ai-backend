package com.dong.yuanmianai.model.vo.assistant;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 对话响应
 */
@Data
public class AiAssistantChatVO implements Serializable {

    private String sessionId;

    private String answer;

    private String source;

    private static final long serialVersionUID = 1L;
}
