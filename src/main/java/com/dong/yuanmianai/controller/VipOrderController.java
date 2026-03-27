package com.dong.yuanmianai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.dong.yuanmianai.common.BaseResponse;
import com.dong.yuanmianai.common.DeleteRequest;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.common.ResultUtils;
import com.dong.yuanmianai.exception.BusinessException;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.model.dto.vipOrder.VipOrderAddRequest;
import com.dong.yuanmianai.model.dto.vipOrder.VipOrderQueryRequest;
import com.dong.yuanmianai.model.dto.vipOrder.VipOrderUpdateRequest;
import com.dong.yuanmianai.model.entity.VipOrder;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.VipOrderVO;
import com.dong.yuanmianai.service.VipOrderService;
import com.dong.yuanmianai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 会员订单接口
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@RestController
@RequestMapping("/vipOrder")
@Slf4j
public class VipOrderController {

    @Resource
    private VipOrderService vipOrderService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建会员订单
     *
     * @param vipOrderAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addVipOrder(@RequestBody VipOrderAddRequest vipOrderAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(vipOrderAddRequest == null, ErrorCode.PARAMS_ERROR);
        VipOrder vipOrder = new VipOrder();
        BeanUtils.copyProperties(vipOrderAddRequest, vipOrder);
        vipOrderService.validVipOrder(vipOrder, true);
        User loginUser = userService.getLoginUser(request);
        vipOrder.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = vipOrderService.save(vipOrder);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newVipOrderId = vipOrder.getId();
        return ResultUtils.success(newVipOrderId);
    }

    /**
     * 删除会员订单
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteVipOrder(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        VipOrder oldVipOrder = vipOrderService.getById(id);
        ThrowUtils.throwIf(oldVipOrder == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!Objects.equals(oldVipOrder.getUserId(), user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = vipOrderService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新会员订单（仅管理员可用）
     *
     * @param vipOrderUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckPermission("vip-order:manage")
    public BaseResponse<Boolean> updateVipOrder(@RequestBody VipOrderUpdateRequest vipOrderUpdateRequest) {
        if (vipOrderUpdateRequest == null || vipOrderUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        VipOrder vipOrder = new VipOrder();
        BeanUtils.copyProperties(vipOrderUpdateRequest, vipOrder);
        // 数据校验
        vipOrderService.validVipOrder(vipOrder, false);
        // 判断是否存在
        long id = vipOrderUpdateRequest.getId();
        VipOrder oldVipOrder = vipOrderService.getById(id);
        ThrowUtils.throwIf(oldVipOrder == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = vipOrderService.updateById(vipOrder);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取会员订单（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<VipOrderVO> getVipOrderVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        VipOrder vipOrder = vipOrderService.getById(id);
        ThrowUtils.throwIf(vipOrder == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(vipOrderService.getVipOrderVO(vipOrder, request));
    }

    /**
     * 分页获取会员订单列表（仅管理员可用）
     *
     * @param vipOrderQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckPermission("vip-order:manage")
    public BaseResponse<Page<VipOrder>> listVipOrderByPage(@RequestBody VipOrderQueryRequest vipOrderQueryRequest) {
        long current = vipOrderQueryRequest.getCurrent();
        long size = vipOrderQueryRequest.getPageSize();
        // 查询数据库
        Page<VipOrder> vipOrderPage = vipOrderService.page(new Page<>(current, size),
                vipOrderService.getQueryWrapper(vipOrderQueryRequest));
        return ResultUtils.success(vipOrderPage);
    }

    /**
     * 分页获取会员订单列表（封装类）
     *
     * @param vipOrderQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<VipOrderVO>> listVipOrderVOByPage(@RequestBody VipOrderQueryRequest vipOrderQueryRequest,
                                                               HttpServletRequest request) {
        long current = vipOrderQueryRequest.getCurrent();
        long size = vipOrderQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<VipOrder> vipOrderPage = vipOrderService.page(new Page<>(current, size),
                vipOrderService.getQueryWrapper(vipOrderQueryRequest));
        // 获取封装类
        return ResultUtils.success(vipOrderService.getVipOrderVOPage(vipOrderPage, request));
    }

    /**
     * 分页获取当前登录用户创建的会员订单列表
     *
     * @param vipOrderQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<VipOrderVO>> listMyVipOrderVOByPage(@RequestBody VipOrderQueryRequest vipOrderQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(vipOrderQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        vipOrderQueryRequest.setUserId(loginUser.getId());
        long current = vipOrderQueryRequest.getCurrent();
        long size = vipOrderQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<VipOrder> vipOrderPage = vipOrderService.page(new Page<>(current, size),
                vipOrderService.getQueryWrapper(vipOrderQueryRequest));
        // 获取封装类
        return ResultUtils.success(vipOrderService.getVipOrderVOPage(vipOrderPage, request));
    }
    

    // endregion
}
