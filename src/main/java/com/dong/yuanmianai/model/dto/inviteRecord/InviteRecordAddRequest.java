package com.dong.yuanmianai.model.dto.inviteRecord;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建邀请记录请求
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Data
public class InviteRecordAddRequest implements Serializable {

    /**
     * 邀请人id
     */
    private Long inviteUserId;

    /**
     * 被邀请人id
     */
    private Long invitedUserId;


    private static final long serialVersionUID = 1L;
}