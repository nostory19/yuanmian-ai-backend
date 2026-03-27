package com.dong.yuanmianai.service;

import com.dong.yuanmianai.model.dto.interview.InterviewAnswerRequest;
import com.dong.yuanmianai.model.dto.interview.InterviewStartRequest;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.InterviewQuestionVO;
import com.dong.yuanmianai.model.vo.InterviewReportVO;

/**
 * 智能面试服务
 */
public interface InterviewService {

    /**
     * 开始面试，创建会话并生成首题
     */
    Long startInterview(InterviewStartRequest request, User loginUser);

    /**
     * 回答问题，触发评分与追问策略
     */
    InterviewQuestionVO answerInterview(InterviewAnswerRequest request, User loginUser);

    /**
     * 获取当前问题
     */
    InterviewQuestionVO getCurrentQuestion(Long sessionId, User loginUser);

    /**
     * 获取面试报告
     */
    InterviewReportVO getInterviewReport(Long sessionId, User loginUser);
}
