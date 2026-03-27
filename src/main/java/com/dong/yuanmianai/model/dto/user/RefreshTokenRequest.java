package com.dong.yuanmianai.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 刷新令牌请求
 */
@Data
public class RefreshTokenRequest implements Serializable {

    private String refreshToken;

    private static final long serialVersionUID = 1L;
}
