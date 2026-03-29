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

    /**
     * agent 决策动作：follow_up / report
     */
    private String nextAction;

    /**
     * evaluator 打分
     */
    private Integer score;

    /**
     * interviewer 题目
     */
    private String question;

    /**
     * evaluator 短板
     */
    private String weakness;

    /**
     * follow-up 追问
     */
    private String followUpQuestion;

    /**
     * report 结果
     */
    private String report;

    /**
     * agent 执行轨迹
     */
    private java.util.List<String> agentTrace;

    private static final long serialVersionUID = 1L;
}
