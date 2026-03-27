package com.dong.yuanmianai.model.dto.questionBankQuestion;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建题库题目请求
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Data
public class QuestionBankQuestionAddRequest implements Serializable {

    /**
     * 题库 id
     */
    private Long questionBankId;

    /**
     * 题目 id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}