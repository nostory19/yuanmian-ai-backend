package com.dong.yuanmianai.model.dto.vipOrder;

import com.dong.yuanmianai.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 查询会员订单请求
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VipOrderQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * id
     */
    private Long notId;

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

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}