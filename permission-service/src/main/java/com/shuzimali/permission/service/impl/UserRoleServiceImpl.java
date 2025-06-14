package com.shuzimali.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.shuzimali.permission.domain.UserRole;
import com.shuzimali.permission.mapper.UserRoleMapper;
import com.shuzimali.permission.service.PermissionService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements PermissionService {
    @Override
    public void bindDefaultRole(Long userId) {

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