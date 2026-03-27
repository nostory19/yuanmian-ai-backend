package com.dong.yuanmianai.model.vo.assistant;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AiAssistantSessionVO implements Serializable {
    private String sessionId;
    private String title;
    private Date createdTime;
    private Date modifyTime;
    private static final long serialVersionUID = 1L;
}
