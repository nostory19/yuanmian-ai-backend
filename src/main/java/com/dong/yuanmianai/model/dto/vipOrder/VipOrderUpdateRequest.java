package com.dong.yuanmianai.model.dto.vipOrder;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 更新会员订单请求
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Data
public class VipOrderUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 会员等级
     */
    private Integer vipLevel;

    /**
     * 支付金额
     */
    private BigDecimal price;

    /**
     * 支付方式 alipay/wechat/code
     */
    private String payType;

    private static final long serialVersionUID = 1L;
}