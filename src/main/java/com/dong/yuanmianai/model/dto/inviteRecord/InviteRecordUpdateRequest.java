package com.dong.yuanmianai.model.dto.inviteRecord;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新邀请记录请求
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Data
public class InviteRecordUpdateRequest implements Serializable {

    /**
     * id
     */
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

    private static final long serialVersionUID = 1L;
}