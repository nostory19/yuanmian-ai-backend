package com.dong.yuanmianai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.constant.CommonConstant;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.mapper.QuestionMapper;
import com.dong.yuanmianai.model.dto.question.QuestionQueryRequest;
import com.dong.yuanmianai.model.entity.Question;
import com.dong.yuanmianai.model.entity.QuestionBankQuestion;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.QuestionVO;
import com.dong.yuanmianai.service.QuestionBankQuestionService;
import com.dong.yuanmianai.service.QuestionService;
import com.dong.yuanmianai.service.UserService;
import com.dong.yuanmianai.utils.SqlUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = question.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        Long notId = questionQueryRequest.getNotId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        String searchText = questionQueryRequest.getSearchText();
        String answer = questionQueryRequest.getAnswer();
        String source = questionQueryRequest.getSource();
        Integer priority = questionQueryRequest.getPriority();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        // 多字段查询
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText).or().like("answer", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(priority), "priority", priority);
        queryWrapper.eq(StringUtils.isNotBlank(source), "source", source);

        // 列表查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        boolean isAsc = sortOrder == null || CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
        if (SqlUtils.validSortField(sortField)) {
            queryWrapper.orderBy(true, isAsc, sortField);
        } else {
            queryWrapper.orderBy(true, false, "createTime");
        }
        return queryWrapper;
    }

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        Long userId = question.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            questionVO.setUser(userService.getUserVO(user));
        }
        return questionVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        List<QuestionVO> questionVOList = questionList.stream().map(QuestionVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).filter(uid -> uid != null && uid > 0).collect(Collectors.toSet());
        Map<Long, User> userIdUserMap = CollUtil.isEmpty(userIdSet) ? Map.of() : userService.listByIds(userIdSet).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        questionVOList.forEach(vo -> {
            Long userId = vo.getUserId();
            if (userId != null && userIdUserMap.containsKey(userId)) {
                vo.setUser(userService.getUserVO(userIdUserMap.get(userId)));
            }
        });
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    @Override
    public void incrementViewNum(Long questionId) {
        if (questionId == null || questionId <= 0) {
            return;
        }
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Question::getId, questionId).setSql("viewNum = IFNULL(viewNum, 0) + 1");
        update(wrapper);
    }

    @Override
    public void incrementThumbNum(Long questionId) {
        if (questionId == null || questionId <= 0) {
            return;
        }
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Question::getId, questionId).setSql("thumbNum = IFNULL(thumbNum, 0) + 1");
        update(wrapper);
    }

    @Override
    public void decrementThumbNum(Long questionId) {
        if (questionId == null || questionId <= 0) {
            return;
        }
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Question::getId, questionId).apply("thumbNum > 0").setSql("thumbNum = thumbNum - 1");
        update(wrapper);
    }

    @Override
    public void incrementFavourNum(Long questionId) {
        if (questionId == null || questionId <= 0) {
            return;
        }
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Question::getId, questionId).setSql("favourNum = IFNULL(favourNum, 0) + 1");
        update(wrapper);
    }

    @Override
    public void decrementFavourNum(Long questionId) {
        if (questionId == null || questionId <= 0) {
            return;
        }
        LambdaUpdateWrapper<Question> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Question::getId, questionId).apply("favourNum > 0").setSql("favourNum = favourNum - 1");
        update(wrapper);
    }

    @Override
    public Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 题目表的查询条件
        QueryWrapper<Question> queryWrapper = this.getQueryWrapper(questionQueryRequest);
        // 根据题库查询题目列表接口
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        if (questionBankId != null) {
            // 查询题库内的题目 id
            // 先根据题库 id 查询题库内的题目 id 列表
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .select(QuestionBankQuestion::getQuestionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            List<QuestionBankQuestion> questionList = questionBankQuestionService.list(lambdaQueryWrapper);
            if (CollUtil.isNotEmpty(questionList)) {
                // 取出题目 id 集合
                Set<Long> questionIdSet = questionList.stream()
                        .map(QuestionBankQuestion::getQuestionId)
                        .collect(Collectors.toSet());
                // 复用原有题目表的查询条件
                queryWrapper.in("id", questionIdSet);
            }
        }
        // 查询数据库
        Page<Question> questionPage = this.page(new Page<>(current, size), queryWrapper);
        return questionPage;
    }

}

