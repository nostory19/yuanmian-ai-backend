package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("refresh_token")
public class RefreshToken implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("refresh_token")
    private String refreshToken;

    @TableField("expire_at")
    private Date expireAt;

    @TableField("created_time")
    private Date createdTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
