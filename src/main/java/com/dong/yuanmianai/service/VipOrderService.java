package com.dong.yuanmianai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.yuanmianai.model.dto.vipOrder.VipOrderQueryRequest;
import com.dong.yuanmianai.model.entity.VipOrder;
import com.dong.yuanmianai.model.vo.VipOrderVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 会员订单服务
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
public interface VipOrderService extends IService<VipOrder> {

    /**
     * 校验数据
     *
     * @param vipOrder
     * @param add 对创建的数据进行校验
     */
    void validVipOrder(VipOrder vipOrder, boolean add);

    /**
     * 获取查询条件
     *
     * @param vipOrderQueryRequest
     * @return
     */
    QueryWrapper<VipOrder> getQueryWrapper(VipOrderQueryRequest vipOrderQueryRequest);
    
    /**
     * 获取会员订单封装
     *
     * @param vipOrder
     * @param request
     * @return
     */
    VipOrderVO getVipOrderVO(VipOrder vipOrder, HttpServletRequest request);

    /**
     * 分页获取会员订单封装
     *
     * @param vipOrderPage
     * @param request
     * @return
     */
    Page<VipOrderVO> getVipOrderVOPage(Page<VipOrder> vipOrderPage, HttpServletRequest request);
}
