package com.dong.yuanmianai.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 面试报告
 */
@Data
public class InterviewReportVO implements Serializable {

    private Long sessionId;

    /**
     * 能力画像
     */
    private Map<String, String> profile;

    /**
     * 改进建议
     */
    private List<String> suggestions;

    /**
     * 平均分
     */
    private Integer avgScore;

    private static final long serialVersionUID = 1L;
}
