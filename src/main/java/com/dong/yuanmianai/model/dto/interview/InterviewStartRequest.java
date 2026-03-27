package com.dong.yuanmianai.model.dto.interview;

import lombok.Data;

import java.io.Serializable;

/**
 * 开始面试请求
 */
@Data
public class InterviewStartRequest implements Serializable {

    /**
     * 面试岗位
     */
    private String jobRole;

    /**
     * 难度：easy / medium / hard
     */
    private String difficulty;

    private static final long serialVersionUID = 1L;
}
