package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 邀请记录表
 * @TableName invite_record
 */
@TableName(value ="invite_record")
@Data
public class InviteRecord implements Serializable {
    /**
     * 记录id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 邀请人id
     */
    private Long inviteUserId;

    /**
     * 被邀请人id
     */
    private Long invitedUserId;

    /**
     * 奖励状态 0-未奖励 1-已奖励
     */
    private Integer rewardStatus;

    /**
     * 奖励类型 vip/points
     */
    private String rewardType;

    /**
     * 奖励值
     */
    private Integer rewardValue;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}