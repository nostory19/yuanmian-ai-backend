package com.dong.yuanmianai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.yuanmianai.model.dto.inviteRecord.InviteRecordQueryRequest;
import com.dong.yuanmianai.model.entity.InviteRecord;
import com.dong.yuanmianai.model.vo.InviteRecordVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 邀请记录服务
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
public interface InviteRecordService extends IService<InviteRecord> {

    /**
     * 校验数据
     *
     * @param inviteRecord
     * @param add 对创建的数据进行校验
     */
    void validInviteRecord(InviteRecord inviteRecord, boolean add);

    /**
     * 获取查询条件
     *
     * @param inviteRecordQueryRequest
     * @return
     */
    QueryWrapper<InviteRecord> getQueryWrapper(InviteRecordQueryRequest inviteRecordQueryRequest);
    
    /**
     * 获取邀请记录封装
     *
     * @param inviteRecord
     * @param request
     * @return
     */
    InviteRecordVO getInviteRecordVO(InviteRecord inviteRecord, HttpServletRequest request);

    /**
     * 分页获取邀请记录封装
     *
     * @param inviteRecordPage
     * @param request
     * @return
     */
    Page<InviteRecordVO> getInviteRecordVOPage(Page<InviteRecord> inviteRecordPage, HttpServletRequest request);
}
