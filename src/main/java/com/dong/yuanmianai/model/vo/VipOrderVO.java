package com.dong.yuanmianai.model.vo;

import cn.hutool.json.JSONUtil;
import com.dong.yuanmianai.model.entity.VipOrder;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 会员订单视图
 *
 * @author <a href="">程序员远行</a>
 * @from <a href="">公众号：所谓远行Misnearch</a>
 */
@Data
public class VipOrderVO implements Serializable {

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
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param vipOrderVO
     * @return
     */
    public static VipOrder voToObj(VipOrderVO vipOrderVO) {
        if (vipOrderVO == null) {
            return null;
        }
        VipOrder vipOrder = new VipOrder();
        BeanUtils.copyProperties(vipOrderVO, vipOrder);

        return vipOrder;
    }

    /**
     * 对象转封装类
     *
     * @param vipOrder
     * @return
     */
    public static VipOrderVO objToVo(VipOrder vipOrder) {
        if (vipOrder == null) {
            return null;
        }
        VipOrderVO vipOrderVO = new VipOrderVO();
        BeanUtils.copyProperties(vipOrder, vipOrderVO);
        return vipOrderVO;
    }
}
