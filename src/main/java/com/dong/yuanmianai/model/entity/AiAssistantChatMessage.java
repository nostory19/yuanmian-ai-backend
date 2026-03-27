package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ai_assistant_chat_messages")
public class AiAssistantChatMessage implements Serializable {

    @TableId("msg_id")
    private String msgId;

    @TableField("session_id")
    private String sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    private String role;

    private String content;

    private String title;

    @TableField("create_time")
    private Date createTime;

    @TableField("modify_time")
    private Date modifyTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
