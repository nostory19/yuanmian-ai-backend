package com.dong.yuanmianai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("role_permission")
public class RolePermission implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("role_code")
    private String roleCode;

    @TableField("perm_code")
    private String permCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
