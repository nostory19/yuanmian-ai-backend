package com.dong.yuanmianai.model.vo;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 已登录用户视图（脱敏）
 *
 * @author <a href="">程序员远行</a>
 * @from <">公众号：所谓远行Misnearch</a>
 **/
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 会员等级 0-普通用户 1-VIP
     */
    private Integer vipLevel;

    /**
     * 会员过期时间
     */
    private Date vipExpireTime;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * accessToken 过期秒数
     */
    private Long expiresIn;

    private static final long serialVersionUID = 1L;
}