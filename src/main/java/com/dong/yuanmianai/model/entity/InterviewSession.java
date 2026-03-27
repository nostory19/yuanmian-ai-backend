package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 面试会话
 */
@TableName(value = "interview_session")
@Data
public class InterviewSession implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String jobRole;

    private String difficulty;

    private String status;

    private Date startTime;

    private Date endTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
