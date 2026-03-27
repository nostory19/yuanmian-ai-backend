package com.dong.yuanmianai.model.dto.user;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * 用户更新请求
 *
 * @author <a href="">程序员远行</a>
 * @from <">公众号：所谓远行Misnearch</a>
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
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
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;


    /**
     * 会员等级 0-普通用户 1-VIP
     */
    private Integer vipLevel;

    /**
     * 会员过期时间
     */
    private Date vipExpireTime;

    private static final long serialVersionUID = 1L;
}