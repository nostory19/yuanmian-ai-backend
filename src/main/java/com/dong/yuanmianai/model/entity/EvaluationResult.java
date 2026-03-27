package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评估结果
 */
@TableName(value = "evaluation_result")
@Data
public class EvaluationResult implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sessionId;

    private Long questionId;

    private Integer score;

    private String feedback;

    /**
     * 多维评分 JSON 字符串
     */
    private String dimension;

    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
