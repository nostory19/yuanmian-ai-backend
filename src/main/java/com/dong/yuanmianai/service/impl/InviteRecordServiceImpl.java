package com.dong.yuanmianai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.constant.CommonConstant;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.mapper.InviteRecordMapper;
import com.dong.yuanmianai.model.dto.inviteRecord.InviteRecordQueryRequest;
import com.dong.yuanmianai.model.entity.InviteRecord;
import com.dong.yuanmianai.model.vo.InviteRecordVO;
import com.dong.yuanmianai.service.InviteRecordService;
import com.dong.yuanmianai.service.UserService;
import com.dong.yuanmianai.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 邀请记录服务实现
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Service
@Slf4j
public class InviteRecordServiceImpl extends ServiceImpl<InviteRecordMapper, InviteRecord> implements InviteRecordService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param inviteRecord
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validInviteRecord(InviteRecord inviteRecord, boolean add) {
        ThrowUtils.throwIf(inviteRecord == null, ErrorCode.PARAMS_ERROR);
        Long inviteUserId = inviteRecord.getInviteUserId();
        Long invitedUserId = inviteRecord.getInvitedUserId();
        if (add) {
            ThrowUtils.throwIf(inviteUserId == null || inviteUserId <= 0, ErrorCode.PARAMS_ERROR, "邀请人id不能为空");
            ThrowUtils.throwIf(invitedUserId == null || invitedUserId <= 0, ErrorCode.PARAMS_ERROR, "被邀请人id不能为空");
        }
        if (inviteRecord.getRewardStatus() != null && inviteRecord.getRewardStatus() != 0 && inviteRecord.getRewardStatus() != 1) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "奖励状态不合法");
        }
    }

    /**
     * 获取查询条件
     *
     * @param inviteRecordQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<InviteRecord> getQueryWrapper(InviteRecordQueryRequest inviteRecordQueryRequest) {
        QueryWrapper<InviteRecord> queryWrapper = new QueryWrapper<>();
        if (inviteRecordQueryRequest == null) {
            return queryWrapper;
        }
        Long id = inviteRecordQueryRequest.getId();
        Long notId = inviteRecordQueryRequest.getNotId();
        Long inviteUserId = inviteRecordQueryRequest.getInviteUserId();
        Long invitedUserId = inviteRecordQueryRequest.getInvitedUserId();
        String sortField = inviteRecordQueryRequest.getSortField();
        String sortOrder = inviteRecordQueryRequest.getSortOrder();
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(inviteUserId), "inviteUserId", inviteUserId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(invitedUserId), "invitedUserId", invitedUserId);
        boolean isAsc = sortOrder == null || CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
        queryWrapper.orderBy(true, isAsc, "createTime");
        return queryWrapper;
    }

    /**
     * 获取邀请记录封装
     *
     * @param inviteRecord
     * @param request
     * @return
     */
    @Override
    public InviteRecordVO getInviteRecordVO(InviteRecord inviteRecord, HttpServletRequest request) {
        InviteRecordVO inviteRecordVO = new InviteRecordVO();
        BeanUtils.copyProperties(inviteRecord, inviteRecordVO);
        return inviteRecordVO;
    }

    /**
     * 分页获取邀请记录封装
     *
     * @param inviteRecordPage
     * @param request
     * @return
     */
    @Override
    public Page<InviteRecordVO> getInviteRecordVOPage(Page<InviteRecord> inviteRecordPage, HttpServletRequest request) {
        List<InviteRecord> inviteRecordList = inviteRecordPage.getRecords();
        Page<InviteRecordVO> inviteRecordVOPage = new Page<>(inviteRecordPage.getCurrent(), inviteRecordPage.getSize(), inviteRecordPage.getTotal());
        if (CollUtil.isEmpty(inviteRecordList)) {
            return inviteRecordVOPage;
        }
        List<InviteRecordVO> inviteRecordVOList = inviteRecordList.stream().map(inviteRecord -> {
            InviteRecordVO vo = new InviteRecordVO();
            BeanUtils.copyProperties(inviteRecord, vo);
            return vo;
        }).collect(Collectors.toList());
        inviteRecordVOPage.setRecords(inviteRecordVOList);
        return inviteRecordVOPage;
    }

}
