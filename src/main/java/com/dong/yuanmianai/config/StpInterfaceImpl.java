package com.dong.yuanmianai.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dong.yuanmianai.mapper.RolePermissionMapper;
import com.dong.yuanmianai.mapper.UserRoleMapper;
import com.dong.yuanmianai.model.entity.RolePermission;
import com.dong.yuanmianai.model.entity.UserRole;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RolePermissionMapper rolePermissionMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> roles = getRoleList(loginId, loginType);
        if (roles.isEmpty()) {
            return List.of();
        }
        List<RolePermission> permissionList = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .in(RolePermission::getRoleCode, roles)
        );
        return permissionList.stream().map(RolePermission::getPermCode).distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        long userId = Long.parseLong(String.valueOf(loginId));
        List<UserRole> roleList = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)
        );
        return roleList.stream().map(UserRole::getRoleCode).distinct().collect(Collectors.toList());
    }
}
