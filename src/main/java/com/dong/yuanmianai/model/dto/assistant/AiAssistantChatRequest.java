package com.dong.yuanmianai.model.dto.assistant;

import lombok.Data;

import java.io.Serializable;

/**
 * AI 对话请求
 */
@Data
public class AiAssistantChatRequest implements Serializable {

    /**
     * 会话 id
     */
    private String sessionId;

    /**
     * 用户 id（可选）
     */
    private String userId;

    /**
     * 用户消息
     */
    private String message;

    private static final long serialVersionUID = 1L;
}
