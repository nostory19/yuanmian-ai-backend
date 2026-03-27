package com.dong.yuanmianai.model.dto.questionBankQuestion;

import lombok.Data;

import java.io.Serializable;

/**
 * @author by hongdou
 * @date 2026/3/23.
 * @DESC: 题库题目关联移除请求类
 */

@Data
public class QuestionBankQuestionRemoveRequest implements Serializable {
    private Long questionBankId;

    private Long questionId;

    public static final long serialVersionUID = 1L;
}
