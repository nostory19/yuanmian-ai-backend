package com.dong.yuanmianai.model.dto.assistant;

import lombok.Data;

import java.io.Serializable;

@Data
public class AiAssistantHistoryRequest implements Serializable {
    private String sessionId;
    private static final long serialVersionUID = 1L;
}
