package com.dong.yuanmianai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.constant.CommonConstant;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.mapper.QuestionBankMapper;
import com.dong.yuanmianai.model.dto.questionBank.QuestionBankQueryRequest;
import com.dong.yuanmianai.model.entity.QuestionBank;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.QuestionBankVO;
import com.dong.yuanmianai.service.QuestionBankService;
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
 * 题库题目服务实现
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Service
@Slf4j
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param questionBank
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestionBank(QuestionBank questionBank, boolean add) {
        ThrowUtils.throwIf(questionBank == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = questionBank.getTitle();
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
     * @param questionBankQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionBank> getQueryWrapper(QuestionBankQueryRequest questionBankQueryRequest) {
        QueryWrapper<QuestionBank> queryWrapper = new QueryWrapper<>();
        if (questionBankQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionBankQueryRequest.getId();
        Long notId = questionBankQueryRequest.getNotId();
        String title = questionBankQueryRequest.getTitle();
        String description = questionBankQueryRequest.getDescription();
        String picture = questionBankQueryRequest.getPicture();
        String searchText = questionBankQueryRequest.getSearchText();
        String sortField = questionBankQueryRequest.getSortField();
        String sortOrder = questionBankQueryRequest.getSortOrder();
        Long userId = questionBankQueryRequest.getUserId();
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("description", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.eq(StringUtils.isNotBlank(picture), "picture", picture);
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
     * 获取题库题目封装
     *
     * @param questionBank
     * @param request
     * @return
     */
    @Override
    public QuestionBankVO getQuestionBankVO(QuestionBank questionBank, HttpServletRequest request) {
        QuestionBankVO questionBankVO = QuestionBankVO.objToVo(questionBank);
        Long userId = questionBank.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            questionBankVO.setUser(userService.getUserVO(user));
        }
        return questionBankVO;
    }

    /**
     * 分页获取题库题目封装
     *
     * @param questionBankPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionBankVO> getQuestionBankVOPage(Page<QuestionBank> questionBankPage, HttpServletRequest request) {
        List<QuestionBank> questionBankList = questionBankPage.getRecords();
        Page<QuestionBankVO> questionBankVOPage = new Page<>(questionBankPage.getCurrent(), questionBankPage.getSize(), questionBankPage.getTotal());
        if (CollUtil.isEmpty(questionBankList)) {
            return questionBankVOPage;
        }
        List<QuestionBankVO> questionBankVOList = questionBankList.stream().map(QuestionBankVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = questionBankList.stream().map(QuestionBank::getUserId).filter(uid -> uid != null && uid > 0).collect(Collectors.toSet());
        Map<Long, User> userIdUserMap = CollUtil.isEmpty(userIdSet) ? Map.of() : userService.listByIds(userIdSet).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        questionBankVOList.forEach(vo -> {
            Long userId = vo.getUserId();
            if (userId != null && userIdUserMap.containsKey(userId)) {
                vo.setUser(userService.getUserVO(userIdUserMap.get(userId)));
            }
        });
        questionBankVOPage.setRecords(questionBankVOList);
        return questionBankVOPage;
    }

}
