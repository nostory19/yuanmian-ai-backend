package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户能力画像
 */
@TableName(value = "user_profile")
@Data
public class UserProfile implements Serializable {

    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 技能向量 JSON 字符串
     */
    private String skillVector;

    private Date updatedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
