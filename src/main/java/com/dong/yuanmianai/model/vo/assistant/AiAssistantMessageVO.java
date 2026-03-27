package com.dong.yuanmianai.model.vo.assistant;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AiAssistantMessageVO implements Serializable {
    private String msgId;
    private String sessionId;
    private String role;
    private String content;
    private Date createTime;
    private static final long serialVersionUID = 1L;
}
