package com.dong.yuanmianai.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.exception.BusinessException;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.mapper.EvaluationResultMapper;
import com.dong.yuanmianai.mapper.InterviewMessageMapper;
import com.dong.yuanmianai.mapper.InterviewSessionMapper;
import com.dong.yuanmianai.mapper.UserProfileMapper;
import com.dong.yuanmianai.model.dto.interview.InterviewAnswerRequest;
import com.dong.yuanmianai.model.dto.interview.InterviewStartRequest;
import com.dong.yuanmianai.model.dto.question.QuestionQueryRequest;
import com.dong.yuanmianai.model.entity.EvaluationResult;
import com.dong.yuanmianai.model.entity.InterviewMessage;
import com.dong.yuanmianai.model.entity.InterviewSession;
import com.dong.yuanmianai.model.entity.Question;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.entity.UserProfile;
import com.dong.yuanmianai.model.vo.InterviewQuestionVO;
import com.dong.yuanmianai.model.vo.InterviewReportVO;
import com.dong.yuanmianai.service.InterviewService;
import com.dong.yuanmianai.service.QuestionService;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 面试编排服务实现
 */
@Service
public class InterviewServiceImpl implements InterviewService {

    private static final String SESSION_STATUS_ACTIVE = "ACTIVE";
    private static final String SESSION_STATUS_FINISHED = "FINISHED";
    private static final int FOLLOW_UP_SCORE_THRESHOLD = 75;
    private static final int MAX_ROUND = 5;

    @Resource
    private InterviewSessionMapper interviewSessionMapper;

    @Resource
    private InterviewMessageMapper interviewMessageMapper;

    @Resource
    private EvaluationResultMapper evaluationResultMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Resource
    private QuestionService questionService;

    @Override
    public Long startInterview(InterviewStartRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(request.getJobRole()), ErrorCode.PARAMS_ERROR, "岗位不能为空");

        InterviewSession session = new InterviewSession();
        session.setUserId(loginUser.getId());
        session.setJobRole(request.getJobRole());
        session.setDifficulty(StringUtils.defaultIfBlank(request.getDifficulty(), "medium"));
        session.setStatus(SESSION_STATUS_ACTIVE);
        session.setStartTime(new Date());
        boolean saved = interviewSessionMapper.insert(session) > 0;
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "创建面试会话失败");

        String firstQuestion = generateQuestion(session, 1);
        saveAgentMessage(session.getId(), firstQuestion, "interviewer");
        return session.getId();
    }

    @Override
    public InterviewQuestionVO answerInterview(InterviewAnswerRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getSessionId() == null || request.getSessionId() <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(request.getAnswer()), ErrorCode.PARAMS_ERROR, "回答不能为空");

        InterviewSession session = checkSessionOwner(request.getSessionId(), loginUser);
        ThrowUtils.throwIf(!SESSION_STATUS_ACTIVE.equals(session.getStatus()), ErrorCode.OPERATION_ERROR, "面试已结束");

        saveUserMessage(session.getId(), request.getAnswer());
        InterviewMessage lastQuestionMsg = getLastAgentQuestion(session.getId());
        ThrowUtils.throwIf(lastQuestionMsg == null, ErrorCode.OPERATION_ERROR, "当前没有可回答的问题");

        EvaluationDecision decision = evaluateAnswer(lastQuestionMsg.getContent(), request.getAnswer());
        saveEvaluation(session.getId(), decision, extractQuestionId(lastQuestionMsg.getContent()));

        int round = countQuestionRound(session.getId());
        if (decision.getScore() < FOLLOW_UP_SCORE_THRESHOLD && round < MAX_ROUND) {
            String followUpQuestion = buildFollowUpQuestion(request.getAnswer(), decision.getReason());
            saveAgentMessage(session.getId(), followUpQuestion, "follow_up");
            return buildCurrentQuestionVO(session, followUpQuestion, "follow_up");
        }

        if (round >= MAX_ROUND) {
            finishSession(session);
            return buildCurrentQuestionVO(session, "本次面试已结束，可查看面试报告。", "report");
        }

        String nextQuestion = generateQuestion(session, round + 1);
        saveAgentMessage(session.getId(), nextQuestion, "interviewer");
        return buildCurrentQuestionVO(session, nextQuestion, "interviewer");
    }

    @Override
    public InterviewQuestionVO getCurrentQuestion(Long sessionId, User loginUser) {
        InterviewSession session = checkSessionOwner(sessionId, loginUser);
        InterviewMessage lastQuestionMsg = getLastAgentQuestion(sessionId);
        ThrowUtils.throwIf(lastQuestionMsg == null, ErrorCode.NOT_FOUND_ERROR, "当前没有问题");
        return buildCurrentQuestionVO(session, lastQuestionMsg.getContent(), lastQuestionMsg.getAgentType());
    }

    @Override
    public InterviewReportVO getInterviewReport(Long sessionId, User loginUser) {
        InterviewSession session = checkSessionOwner(sessionId, loginUser);
        if (SESSION_STATUS_ACTIVE.equals(session.getStatus())) {
            finishSession(session);
            session.setStatus(SESSION_STATUS_FINISHED);
        }
        List<EvaluationResult> resultList = evaluationResultMapper.selectList(
                new LambdaQueryWrapper<EvaluationResult>()
                        .eq(EvaluationResult::getSessionId, sessionId)
                        .orderByAsc(EvaluationResult::getCreateTime)
        );
        ThrowUtils.throwIf(resultList.isEmpty(), ErrorCode.NOT_FOUND_ERROR, "暂无评估记录");

        int total = 0;
        Map<String, Integer> dimScoreMap = new HashMap<>();
        for (EvaluationResult result : resultList) {
            total += result.getScore() == null ? 0 : result.getScore();
            if (StringUtils.isNotBlank(result.getDimension())) {
                JSONObject jsonObject = JSONUtil.parseObj(result.getDimension());
                for (String key : jsonObject.keySet()) {
                    int value = parseInt(jsonObject.get(key), 0);
                    dimScoreMap.merge(key, value, Integer::sum);
                }
            }
        }
        int avgScore = total / resultList.size();
        Map<String, String> profile = buildProfile(dimScoreMap, resultList.size());
        List<String> suggestions = buildSuggestions(profile);

        updateUserProfile(loginUser.getId(), profile);

        InterviewReportVO reportVO = new InterviewReportVO();
        reportVO.setSessionId(sessionId);
        reportVO.setAvgScore(avgScore);
        reportVO.setProfile(profile);
        reportVO.setSuggestions(suggestions);
        return reportVO;
    }

    private InterviewSession checkSessionOwner(Long sessionId, User loginUser) {
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(sessionId == null || sessionId <= 0, ErrorCode.PARAMS_ERROR);
        InterviewSession session = interviewSessionMapper.selectById(sessionId);
        ThrowUtils.throwIf(session == null, ErrorCode.NOT_FOUND_ERROR, "面试会话不存在");
        if (!loginUser.getId().equals(session.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return session;
    }

    private void finishSession(InterviewSession session) {
        session.setStatus(SESSION_STATUS_FINISHED);
        session.setEndTime(new Date());
        interviewSessionMapper.updateById(session);
    }

    private InterviewMessage getLastAgentQuestion(Long sessionId) {
        return interviewMessageMapper.selectOne(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .eq(InterviewMessage::getRole, "agent")
                        .orderByDesc(InterviewMessage::getCreateTime)
                        .last("limit 1")
        );
    }

    private int countQuestionRound(Long sessionId) {
        Long count = interviewMessageMapper.selectCount(
                new LambdaQueryWrapper<InterviewMessage>()
                        .eq(InterviewMessage::getSessionId, sessionId)
                        .eq(InterviewMessage::getRole, "agent")
        );
        return count == null ? 0 : count.intValue();
    }

    private void saveUserMessage(Long sessionId, String content) {
        InterviewMessage message = new InterviewMessage();
        message.setSessionId(sessionId);
        message.setRole("user");
        message.setAgentType("candidate");
        message.setContent(content);
        message.setCreateTime(new Date());
        interviewMessageMapper.insert(message);
    }

    private void saveAgentMessage(Long sessionId, String content, String agentType) {
        InterviewMessage message = new InterviewMessage();
        message.setSessionId(sessionId);
        message.setRole("agent");
        message.setAgentType(agentType);
        message.setContent(content);
        message.setCreateTime(new Date());
        interviewMessageMapper.insert(message);
    }

    private void saveEvaluation(Long sessionId, EvaluationDecision decision, Long questionId) {
        EvaluationResult result = new EvaluationResult();
        result.setSessionId(sessionId);
        result.setQuestionId(questionId);
        result.setScore(decision.getScore());
        result.setFeedback(decision.getReason() + "；建议：" + decision.getImprovement());
        result.setDimension(JSONUtil.toJsonStr(decision.getDimension()));
        result.setCreateTime(new Date());
        evaluationResultMapper.insert(result);
    }

    private String generateQuestion(InterviewSession session, int round) {
        QuestionQueryRequest queryRequest = new QuestionQueryRequest();
        queryRequest.setPageSize(10);
        queryRequest.setCurrent(1);
        queryRequest.setSearchText(session.getJobRole());
        List<Question> questionList = questionService.listQuestionByPage(queryRequest).getRecords();
        if (!questionList.isEmpty()) {
            Question selected = questionList.get((round - 1) % questionList.size());
            return "[QID:" + selected.getId() + "] " + selected.getTitle() + "。请结合工程实践回答。";
        }
        return "请介绍一个你在 " + session.getJobRole() + " 方向遇到的复杂问题，并说明你的解决思路。";
    }

    private EvaluationDecision evaluateAnswer(String question, String answer) {
        int lengthScore = Math.min(100, Math.max(40, answer.length() / 3));
        boolean hasComplexity = StringUtils.containsIgnoreCase(answer, "复杂度")
                || StringUtils.containsIgnoreCase(answer, "O(");
        int score = hasComplexity ? Math.min(100, lengthScore + 8) : lengthScore;

        Map<String, Integer> dimensions = new HashMap<>();
        dimensions.put("data_structure", Math.min(100, score + 3));
        dimensions.put("system_design", Math.max(45, score - 10));
        dimensions.put("expression", Math.min(100, score));

        EvaluationDecision decision = new EvaluationDecision();
        decision.setScore(score);
        decision.setReason(hasComplexity ? "思路较完整，复杂度意识较好" : "基础思路可行，但复杂度分析不足");
        decision.setImprovement(hasComplexity ? "可进一步补充边界条件和容错策略" : "建议补充时间复杂度与空间复杂度分析");
        decision.setDimension(dimensions);
        return decision;
    }

    private String buildFollowUpQuestion(String answer, String reason) {
        String keyPoint = answer.length() > 30 ? answer.substring(0, 30) + "..." : answer;
        return "你刚刚提到「" + keyPoint + "」，基于该方案请进一步说明在高并发场景下如何保证稳定性？（评估依据：" + reason + "）";
    }

    private Long extractQuestionId(String questionContent) {
        if (StringUtils.isBlank(questionContent) || !questionContent.startsWith("[QID:")) {
            return null;
        }
        int end = questionContent.indexOf("]");
        if (end <= 5) {
            return null;
        }
        try {
            return Long.parseLong(questionContent.substring(5, end));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private InterviewQuestionVO buildCurrentQuestionVO(InterviewSession session, String question, String agentType) {
        InterviewQuestionVO vo = new InterviewQuestionVO();
        vo.setSessionId(session.getId());
        vo.setStatus(session.getStatus());
        vo.setQuestion(question);
        vo.setAgentType(agentType);
        return vo;
    }

    private Map<String, String> buildProfile(Map<String, Integer> dimScoreMap, int count) {
        Map<String, String> profile = new HashMap<>();
        profile.put("数据结构", scoreLevel(dimScoreMap.getOrDefault("data_structure", 60) / count));
        profile.put("系统设计", scoreLevel(dimScoreMap.getOrDefault("system_design", 60) / count));
        profile.put("表达能力", scoreLevel(dimScoreMap.getOrDefault("expression", 60) / count));
        return profile;
    }

    private List<String> buildSuggestions(Map<String, String> profile) {
        List<String> suggestions = new ArrayList<>();
        if ("较弱".equals(profile.get("系统设计"))) {
            suggestions.add("多练习分布式系统与高并发场景题");
        }
        if ("中等".equals(profile.get("表达能力")) || "较弱".equals(profile.get("表达能力"))) {
            suggestions.add("提升表达结构化能力，先结论后论证");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("继续保持当前训练节奏，逐步提升题目难度");
        }
        return suggestions;
    }

    private String scoreLevel(int score) {
        if (score >= 80) {
            return "良好";
        }
        if (score >= 60) {
            return "中等";
        }
        return "较弱";
    }

    private void updateUserProfile(Long userId, Map<String, String> profile) {
        UserProfile userProfile = userProfileMapper.selectById(userId);
        if (userProfile == null) {
            userProfile = new UserProfile();
            userProfile.setUserId(userId);
        }
        userProfile.setSkillVector(JSONUtil.toJsonStr(profile));
        userProfile.setUpdatedTime(new Date());
        if (userProfileMapper.selectById(userId) == null) {
            userProfileMapper.insert(userProfile);
        } else {
            userProfileMapper.updateById(userProfile);
        }
    }

    private int parseInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Data
    private static class EvaluationDecision {
        private Integer score;
        private String reason;
        private String improvement;
        private Map<String, Integer> dimension;
    }
}
