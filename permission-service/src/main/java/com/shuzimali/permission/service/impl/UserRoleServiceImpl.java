package com.shuzimali.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.shuzimali.permission.domain.Roles;
import com.shuzimali.permission.domain.UserRole;
import com.shuzimali.permission.mapper.UserRoleMapper;
import com.shuzimali.permission.service.PermissionService;
import com.shuzimali.permission.service.RolesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
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
        log.info("【获取用户角色】userId={}", userId);
        UserRole userRole = lambdaQuery().eq(UserRole::getUserId, userId).one();
        Roles roles = rolesService.getById(userRole.getRoleId());
        log.info("【获取用户角色】roles={}", roles);
        return roles.getRoleCode();
    }

    @Override
    public void upgradeToAdmin(Long userId) {
        log.info("【升级用户】userId={}", userId);
        lambdaUpdate().eq(UserRole::getUserId, userId).set(UserRole::getRoleId, 2).update();
    }

    @Override
    public void downgradeToUser(Long userId) {
        log.info("【降级用户】userId={}", userId);
        lambdaUpdate().eq(UserRole::getUserId, userId).set(UserRole::getRoleId, 3).update();
    }

    @Override
    public List<Long> getNormalUsers() {
        return lambdaQuery().eq(UserRole::getRoleId, 3).list()
                .stream()
                .map(UserRole::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    public void processedCallback(String transactionId) {

    }
}