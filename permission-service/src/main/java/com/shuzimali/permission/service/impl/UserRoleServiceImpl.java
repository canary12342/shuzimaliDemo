package com.shuzimali.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.shuzimali.permission.domain.Roles;
import com.shuzimali.permission.domain.UserRole;
import com.shuzimali.permission.mapper.UserRoleMapper;
import com.shuzimali.permission.service.PermissionService;
import com.shuzimali.permission.service.RolesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements PermissionService {
    private final RolesService rolesService;
    @Override
    public void bindDefaultRole(Long userId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        Roles roles = rolesService.lambdaQuery().eq(Roles::getRoleCode, "user").one();
        userRole.setRoleId(roles.getRoleId());
        save(userRole);
    }

    @Override
    public String getUserRoleCode(Long userId) {
        return "";
    }

    @Override
    public void upgradeToAdmin(Long userId) {

    }

    @Override
    public void downgradeToUser(Long userId) {

    }
}