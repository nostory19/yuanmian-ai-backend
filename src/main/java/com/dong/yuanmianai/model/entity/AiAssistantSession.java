package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("ai_assistant_sessions")
public class AiAssistantSession implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("session_id")
    private String sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("user_name")
    private String userName;

    @TableField("created_time")
    private Date createdTime;

    @TableField("modify_time")
    private Date modifyTime;

    private String title;

    @TableField("daily_routes")
    private String dailyRoutes;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
