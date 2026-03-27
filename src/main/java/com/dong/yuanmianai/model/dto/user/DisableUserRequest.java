package com.dong.yuanmianai.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class DisableUserRequest implements Serializable {

    @NotNull
    private Long userId;

    private static final long serialVersionUID = 1L;
}
