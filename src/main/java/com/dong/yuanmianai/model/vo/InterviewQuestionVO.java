package com.dong.yuanmianai.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前面试问题
 */
@Data
public class InterviewQuestionVO implements Serializable {

    private Long sessionId;

    private String status;

    private String question;

    private String agentType;

    private static final long serialVersionUID = 1L;
}
