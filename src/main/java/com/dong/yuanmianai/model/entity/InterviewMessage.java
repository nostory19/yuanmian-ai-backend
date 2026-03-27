package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 面试消息记录
 */
@TableName(value = "interview_message")
@Data
public class InterviewMessage implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long sessionId;

    private String role;

    private String content;

    private String agentType;

    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
