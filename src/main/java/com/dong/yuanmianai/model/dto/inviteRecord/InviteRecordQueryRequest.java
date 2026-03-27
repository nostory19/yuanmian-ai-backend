package com.dong.yuanmianai.model.dto.inviteRecord;

import com.dong.yuanmianai.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询邀请记录请求
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InviteRecordQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * id
     */
    private Long notId;

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