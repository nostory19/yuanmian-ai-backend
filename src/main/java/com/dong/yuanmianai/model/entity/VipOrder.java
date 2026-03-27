package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 会员订单表
 * @TableName vip_order
 */
@TableName(value ="vip_order")
@Data
public class VipOrder implements Serializable {
    /**
     * 订单id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 会员等级
     */
    private Integer vipLevel;

    /**
     * 会员天数
     */
    private Integer vipDays;

    /**
     * 支付金额
     */
    private BigDecimal price;

    /**
     * 支付方式 alipay/wechat/code
     */
    private String payType;

    /**
     * 支付状态 0-未支付 1-已支付
     */
    private Integer payStatus;

    /**
     * 会员到期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}