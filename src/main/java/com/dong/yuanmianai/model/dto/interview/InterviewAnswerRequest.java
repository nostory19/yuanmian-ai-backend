package com.dong.yuanmianai.model.dto.interview;

import lombok.Data;

import java.io.Serializable;

/**
 * 面试回答请求
 */
@Data
public class InterviewAnswerRequest implements Serializable {

    /**
     * 会话 id
     */
    private Long sessionId;

    /**
     * 候选人回答
     */
    private String answer;

    private static final long serialVersionUID = 1L;
}
