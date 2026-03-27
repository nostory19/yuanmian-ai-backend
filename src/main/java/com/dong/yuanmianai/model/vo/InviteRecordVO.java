package com.dong.yuanmianai.model.vo;

import cn.hutool.json.JSONUtil;
import com.dong.yuanmianai.model.entity.InviteRecord;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 邀请记录视图
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Data
public class InviteRecordVO implements Serializable {

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 封装类转对象
     *
     * @param inviteRecordVO
     * @return
     */
    public static InviteRecord voToObj(InviteRecordVO inviteRecordVO) {
        if (inviteRecordVO == null) {
            return null;
        }
        InviteRecord inviteRecord = new InviteRecord();
        BeanUtils.copyProperties(inviteRecordVO, inviteRecord);
        return inviteRecord;
    }

    /**
     * 对象转封装类
     *
     * @param inviteRecord
     * @return
     */
    public static InviteRecordVO objToVo(InviteRecord inviteRecord) {
        if (inviteRecord == null) {
            return null;
        }
        InviteRecordVO inviteRecordVO = new InviteRecordVO();
        BeanUtils.copyProperties(inviteRecord, inviteRecordVO);
        return inviteRecordVO;
    }
}
