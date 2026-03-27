package com.dong.yuanmianai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.dong.yuanmianai.common.BaseResponse;
import com.dong.yuanmianai.common.DeleteRequest;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.common.ResultUtils;
import com.dong.yuanmianai.exception.BusinessException;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.model.dto.inviteRecord.InviteRecordAddRequest;
import com.dong.yuanmianai.model.dto.inviteRecord.InviteRecordQueryRequest;
import com.dong.yuanmianai.model.dto.inviteRecord.InviteRecordUpdateRequest;
import com.dong.yuanmianai.model.entity.InviteRecord;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.InviteRecordVO;
import com.dong.yuanmianai.service.InviteRecordService;
import com.dong.yuanmianai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 邀请记录接口
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@RestController
@RequestMapping("/inviteRecord")
@Slf4j
public class InviteRecordController {

    @Resource
    private InviteRecordService inviteRecordService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建邀请记录
     *
     * @param inviteRecordAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInviteRecord(@RequestBody InviteRecordAddRequest inviteRecordAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(inviteRecordAddRequest == null, ErrorCode.PARAMS_ERROR);
        InviteRecord inviteRecord = new InviteRecord();
        BeanUtils.copyProperties(inviteRecordAddRequest, inviteRecord);
        inviteRecordService.validInviteRecord(inviteRecord, true);
        // 填充默认值：当前登录用户为邀请人
        User loginUser = userService.getLoginUser(request);
        inviteRecord.setInviteUserId(loginUser.getId());
        // 写入数据库
        boolean result = inviteRecordService.save(inviteRecord);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newInviteRecordId = inviteRecord.getId();
        return ResultUtils.success(newInviteRecordId);
    }

    /**
     * 删除邀请记录
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInviteRecord(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InviteRecord oldInviteRecord = inviteRecordService.getById(id);
        ThrowUtils.throwIf(oldInviteRecord == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅邀请人本人或管理员可删除
        if (!Objects.equals(oldInviteRecord.getInviteUserId(), user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = inviteRecordService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新邀请记录（仅管理员可用）
     *
     * @param inviteRecordUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckPermission("invite-record:manage")
    public BaseResponse<Boolean> updateInviteRecord(@RequestBody InviteRecordUpdateRequest inviteRecordUpdateRequest) {
        if (inviteRecordUpdateRequest == null || inviteRecordUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InviteRecord inviteRecord = new InviteRecord();
        BeanUtils.copyProperties(inviteRecordUpdateRequest, inviteRecord);
        // 数据校验
        inviteRecordService.validInviteRecord(inviteRecord, false);
        // 判断是否存在
        long id = inviteRecordUpdateRequest.getId();
        InviteRecord oldInviteRecord = inviteRecordService.getById(id);
        ThrowUtils.throwIf(oldInviteRecord == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = inviteRecordService.updateById(inviteRecord);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取邀请记录（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<InviteRecordVO> getInviteRecordVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        InviteRecord inviteRecord = inviteRecordService.getById(id);
        ThrowUtils.throwIf(inviteRecord == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(inviteRecordService.getInviteRecordVO(inviteRecord, request));
    }

    /**
     * 分页获取邀请记录列表（仅管理员可用）
     *
     * @param inviteRecordQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckPermission("invite-record:manage")
    public BaseResponse<Page<InviteRecord>> listInviteRecordByPage(@RequestBody InviteRecordQueryRequest inviteRecordQueryRequest) {
        long current = inviteRecordQueryRequest.getCurrent();
        long size = inviteRecordQueryRequest.getPageSize();
        // 查询数据库
        Page<InviteRecord> inviteRecordPage = inviteRecordService.page(new Page<>(current, size),
                inviteRecordService.getQueryWrapper(inviteRecordQueryRequest));
        return ResultUtils.success(inviteRecordPage);
    }

    /**
     * 分页获取邀请记录列表（封装类）
     *
     * @param inviteRecordQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<InviteRecordVO>> listInviteRecordVOByPage(@RequestBody InviteRecordQueryRequest inviteRecordQueryRequest,
                                                               HttpServletRequest request) {
        long current = inviteRecordQueryRequest.getCurrent();
        long size = inviteRecordQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<InviteRecord> inviteRecordPage = inviteRecordService.page(new Page<>(current, size),
                inviteRecordService.getQueryWrapper(inviteRecordQueryRequest));
        // 获取封装类
        return ResultUtils.success(inviteRecordService.getInviteRecordVOPage(inviteRecordPage, request));
    }

    /**
     * 分页获取当前登录用户创建的邀请记录列表
     *
     * @param inviteRecordQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<InviteRecordVO>> listMyInviteRecordVOByPage(@RequestBody InviteRecordQueryRequest inviteRecordQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(inviteRecordQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户作为邀请人的记录
        User loginUser = userService.getLoginUser(request);
        inviteRecordQueryRequest.setInviteUserId(loginUser.getId());
        long current = inviteRecordQueryRequest.getCurrent();
        long size = inviteRecordQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<InviteRecord> inviteRecordPage = inviteRecordService.page(new Page<>(current, size),
                inviteRecordService.getQueryWrapper(inviteRecordQueryRequest));
        // 获取封装类
        return ResultUtils.success(inviteRecordService.getInviteRecordVOPage(inviteRecordPage, request));
    }



    // endregion
}
