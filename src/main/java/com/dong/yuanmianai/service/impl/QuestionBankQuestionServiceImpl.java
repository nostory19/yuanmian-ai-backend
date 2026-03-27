package com.dong.yuanmianai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.constant.CommonConstant;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.mapper.QuestionBankQuestionMapper;
import com.dong.yuanmianai.model.dto.questionBankQuestion.QuestionBankQuestionQueryRequest;
import com.dong.yuanmianai.model.entity.Question;
import com.dong.yuanmianai.model.entity.QuestionBank;
import com.dong.yuanmianai.model.entity.QuestionBankQuestion;

import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.QuestionBankQuestionVO;
import com.dong.yuanmianai.service.QuestionBankQuestionService;
import com.dong.yuanmianai.service.QuestionBankService;
import com.dong.yuanmianai.service.QuestionService;
import com.dong.yuanmianai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题库题目服务实现
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Service
@Slf4j
public class QuestionBankQuestionServiceImpl extends ServiceImpl<QuestionBankQuestionMapper, QuestionBankQuestion> implements QuestionBankQuestionService {

    @Resource
    private UserService userService;
    @Resource
    @Lazy
    private QuestionService questionService;
    @Resource
    private QuestionBankService questionBankService;

    /**
     * 校验数据
     *
     * @param questionBankQuestion
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBankQuestion(QuestionBankQuestion questionBankQuestion, boolean add) {
        ThrowUtils.throwIf(questionBankQuestion == null, ErrorCode.PARAMS_ERROR);
//        if (add) {
//            ThrowUtils.throwIf(questionBankQuestion.getQuestionBankId() == null || questionBankQuestion.getQuestionBankId() <= 0, ErrorCode.PARAMS_ERROR, "题库id不能为空");
//            ThrowUtils.throwIf(questionBankQuestion.getQuestionId() == null || questionBankQuestion.getQuestionId() <= 0, ErrorCode.PARAMS_ERROR, "题目id不能为空");
//            // 这里并没有传入userId，后续会根据登录用户id进行赋值
////            ThrowUtils.throwIf(questionBankQuestion.getUserId() == null || questionBankQuestion.getUserId() <= 0, ErrorCode.PARAMS_ERROR, "用户id不能为空");

//        }
        // 这里需要确保题库，题目都存在
        Long questionId = questionBankQuestion.getQuestionId();
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR, "题目不存在");
        }

        Long questionBankId = questionBankQuestion.getQuestionBankId();
        if (questionBankId != null) {
            QuestionBank questionBank = questionBankService.getById(questionBankId);
            ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR, "题库不存在");
        }

    }

    /**
     * 获取查询条件
     *
     * @param questionBankQuestionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBankQuestion> getQueryWrapper(QuestionBankQuestionQueryRequest questionBankQuestionQueryRequest) {
        QueryWrapper<QuestionBankQuestion> queryWrapper = new QueryWrapper<>();
        if (questionBankQuestionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionBankQuestionQueryRequest.getId();
        Long notId = questionBankQuestionQueryRequest.getNotId();
        Long questionBankId = questionBankQuestionQueryRequest.getQuestionBankId();
        Long questionId = questionBankQuestionQueryRequest.getQuestionId();
        Long userId = questionBankQuestionQueryRequest.getUserId();
        String sortOrder = questionBankQuestionQueryRequest.getSortOrder();
        // Mybatis plus会按照实体驼峰命名规则自动映射数据库字段
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionBankId), "questionBankId", questionBankId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        boolean isAsc = sortOrder == null || CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
        queryWrapper.orderBy(true, isAsc, "createTime");
        return queryWrapper;
    }

    /**
     * 获取题库题目封装
     *
     * @param questionBankQuestion
     * @param request
     * @return
     */
    @Override
    public QuestionBankQuestionVO getQuestionBankQuestionVO(QuestionBankQuestion questionBankQuestion, HttpServletRequest request) {
        QuestionBankQuestionVO questionBankQuestionVO = QuestionBankQuestionVO.objToVo(questionBankQuestion);
        Long userId = questionBankQuestion.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            questionBankQuestionVO.setUser(userService.getUserVO(user));
        }
        return questionBankQuestionVO;
    }

    /**
     * 分页获取题库题目封装
     *
     * @param questionBankQuestionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankQuestionVO> getQuestionBankQuestionVOPage(Page<QuestionBankQuestion> questionBankQuestionPage, HttpServletRequest request) {
        List<QuestionBankQuestion> questionBankQuestionList = questionBankQuestionPage.getRecords();
        Page<QuestionBankQuestionVO> questionBankQuestionVOPage = new Page<>(questionBankQuestionPage.getCurrent(), questionBankQuestionPage.getSize(), questionBankQuestionPage.getTotal());
        if (CollUtil.isEmpty(questionBankQuestionList)) {
            return questionBankQuestionVOPage;
        }
        List<QuestionBankQuestionVO> questionBankQuestionVOList = questionBankQuestionList.stream().map(QuestionBankQuestionVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = questionBankQuestionList.stream().map(QuestionBankQuestion::getUserId).filter(uid -> uid != null && uid > 0).collect(Collectors.toSet());
        Map<Long, User> userIdUserMap = CollUtil.isEmpty(userIdSet) ? Map.of() : userService.listByIds(userIdSet).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        questionBankQuestionVOList.forEach(vo -> {
            Long userId = vo.getUserId();
            if (userId != null && userIdUserMap.containsKey(userId)) {
                vo.setUser(userService.getUserVO(userIdUserMap.get(userId)));
            }
        });
        questionBankQuestionVOPage.setRecords(questionBankQuestionVOList);
        return questionBankQuestionVOPage;
    }

}
