package com.dong.yuanmianai.controller;

import com.dong.yuanmianai.common.BaseResponse;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.common.ResultUtils;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.model.dto.interview.InterviewAnswerRequest;
import com.dong.yuanmianai.model.dto.interview.InterviewStartRequest;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.InterviewQuestionVO;
import com.dong.yuanmianai.model.vo.InterviewReportVO;
import com.dong.yuanmianai.service.InterviewService;
import com.dong.yuanmianai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能面试接口
 */
@RestController
@RequestMapping("/interview")
@Slf4j
public class InterviewController {

    @Resource
    private InterviewService interviewService;

    @Resource
    private UserService userService;

    /**
     * 开始面试
     */
    @PostMapping("/start")
    public BaseResponse<Long> startInterview(@RequestBody InterviewStartRequest interviewStartRequest,
                                             HttpServletRequest request) {
        ThrowUtils.throwIf(interviewStartRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long sessionId = interviewService.startInterview(interviewStartRequest, loginUser);
        return ResultUtils.success(sessionId);
    }

    /**
     * 用户回答
     */
    @PostMapping("/answer")
    public BaseResponse<InterviewQuestionVO> answerInterview(@RequestBody InterviewAnswerRequest interviewAnswerRequest,
                                                             HttpServletRequest request) {
        ThrowUtils.throwIf(interviewAnswerRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        InterviewQuestionVO questionVO = interviewService.answerInterview(interviewAnswerRequest, loginUser);
        return ResultUtils.success(questionVO);
    }

    /**
     * 获取当前问题
     */
    @GetMapping("/current")
    public BaseResponse<InterviewQuestionVO> getCurrentQuestion(@RequestParam("sessionId") Long sessionId,
                                                                HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        InterviewQuestionVO questionVO = interviewService.getCurrentQuestion(sessionId, loginUser);
        return ResultUtils.success(questionVO);
    }

    /**
     * 获取面试报告
     */
    @GetMapping("/report")
    public BaseResponse<InterviewReportVO> getInterviewReport(@RequestParam("sessionId") Long sessionId,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        InterviewReportVO reportVO = interviewService.getInterviewReport(sessionId, loginUser);
        return ResultUtils.success(reportVO);
    }
}
