package com.dong.yuanmianai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.constant.CommonConstant;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.mapper.VipOrderMapper;
import com.dong.yuanmianai.model.dto.vipOrder.VipOrderQueryRequest;
import com.dong.yuanmianai.model.entity.VipOrder;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.VipOrderVO;
import com.dong.yuanmianai.service.VipOrderService;
import com.dong.yuanmianai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会员订单服务实现
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Service
@Slf4j
public class VipOrderServiceImpl extends ServiceImpl<VipOrderMapper, VipOrder> implements VipOrderService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param vipOrder
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validVipOrder(VipOrder vipOrder, boolean add) {
        ThrowUtils.throwIf(vipOrder == null, ErrorCode.PARAMS_ERROR);
        if (add) {
            ThrowUtils.throwIf(vipOrder.getUserId() == null || vipOrder.getUserId() <= 0, ErrorCode.PARAMS_ERROR, "用户id不能为空");
            ThrowUtils.throwIf(vipOrder.getVipLevel() == null, ErrorCode.PARAMS_ERROR, "会员等级不能为空");
            ThrowUtils.throwIf(vipOrder.getPrice() == null || vipOrder.getPrice().compareTo(BigDecimal.ZERO) < 0, ErrorCode.PARAMS_ERROR, "支付金额不合法");
            ThrowUtils.throwIf(StringUtils.isBlank(vipOrder.getPayType()), ErrorCode.PARAMS_ERROR, "支付方式不能为空");
        }
        if (vipOrder.getPayStatus() != null && vipOrder.getPayStatus() != 0 && vipOrder.getPayStatus() != 1) {
            ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "支付状态不合法");
        }
    }

    /**
     * 获取查询条件
     *
     * @param vipOrderQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<VipOrder> getQueryWrapper(VipOrderQueryRequest vipOrderQueryRequest) {
        QueryWrapper<VipOrder> queryWrapper = new QueryWrapper<>();
        if (vipOrderQueryRequest == null) {
            return queryWrapper;
        }
        Long id = vipOrderQueryRequest.getId();
        Long notId = vipOrderQueryRequest.getNotId();
        Long userId = vipOrderQueryRequest.getUserId();
        Integer vipLevel = vipOrderQueryRequest.getVipLevel();
        BigDecimal price = vipOrderQueryRequest.getPrice();
        String payType = vipOrderQueryRequest.getPayType();
        String sortOrder = vipOrderQueryRequest.getSortOrder();
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(vipLevel), "vipLevel", vipLevel);
        queryWrapper.eq(ObjectUtils.isNotEmpty(price), "price", price);
        queryWrapper.eq(StringUtils.isNotBlank(payType), "payType", payType);
        boolean isAsc = sortOrder == null || CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
        queryWrapper.orderBy(true, isAsc, "createTime");
        return queryWrapper;
    }

    /**
     * 获取会员订单封装
     *
     * @param vipOrder
     * @param request
     * @return
     */
    @Override
    public VipOrderVO getVipOrderVO(VipOrder vipOrder, HttpServletRequest request) {
        VipOrderVO vipOrderVO = new VipOrderVO();
        BeanUtils.copyProperties(vipOrder, vipOrderVO);
        Long userId = vipOrder.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            vipOrderVO.setUser(userService.getUserVO(user));
        }
        return vipOrderVO;
    }

    /**
     * 分页获取会员订单封装
     *
     * @param vipOrderPage
     * @param request
     * @return
     */
    @Override
    public Page<VipOrderVO> getVipOrderVOPage(Page<VipOrder> vipOrderPage, HttpServletRequest request) {
        List<VipOrder> vipOrderList = vipOrderPage.getRecords();
        Page<VipOrderVO> vipOrderVOPage = new Page<>(vipOrderPage.getCurrent(), vipOrderPage.getSize(), vipOrderPage.getTotal());
        if (CollUtil.isEmpty(vipOrderList)) {
            return vipOrderVOPage;
        }
        List<VipOrderVO> vipOrderVOList = vipOrderList.stream().map(vipOrder -> {
            VipOrderVO vo = new VipOrderVO();
            BeanUtils.copyProperties(vipOrder, vo);
            return vo;
        }).collect(Collectors.toList());
        Set<Long> userIdSet = vipOrderList.stream().map(VipOrder::getUserId).filter(uid -> uid != null && uid > 0).collect(Collectors.toSet());
        Map<Long, User> userIdUserMap = CollUtil.isEmpty(userIdSet) ? Map.of() : userService.listByIds(userIdSet).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        vipOrderVOList.forEach(vo -> {
            Long userId = vo.getUserId();
            if (userId != null && userIdUserMap.containsKey(userId)) {
                vo.setUser(userService.getUserVO(userIdUserMap.get(userId)));
            }
        });
        vipOrderVOPage.setRecords(vipOrderVOList);
        return vipOrderVOPage;
    }

}
