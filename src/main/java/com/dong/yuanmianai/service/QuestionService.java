package com.dong.yuanmianai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.yuanmianai.model.dto.question.QuestionQueryRequest;
import com.dong.yuanmianai.model.entity.Question;
import com.dong.yuanmianai.model.vo.QuestionVO;
import jakarta.servlet.http.HttpServletRequest;


/**
 * 题目服务
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     *
     * @param question
     * @param add 对创建的数据进行校验
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);
    
    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

    /**
     * 增加题目浏览量
     *
     * @param questionId 题目 id
     */
    void incrementViewNum(Long questionId);

    /**
     * 题目点赞数 +1
     *
     * @param questionId 题目 id
     */
    void incrementThumbNum(Long questionId);

    /**
     * 题目点赞数 -1（不小于 0）
     *
     * @param questionId 题目 id
     */
    void decrementThumbNum(Long questionId);

    /**
     * 题目收藏数 +1
     *
     * @param questionId 题目 id
     */
    void incrementFavourNum(Long questionId);

    /**
     * 题目收藏数 -1（不小于 0）
     *
     * @param questionId 题目 id
     */
    void decrementFavourNum(Long questionId);


    /**
     * 分页获取题目列表（封装类）
     * @param questionQueryRequest
     * @return
     */
    Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest);
}
