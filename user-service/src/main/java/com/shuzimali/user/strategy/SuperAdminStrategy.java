package com.shuzimali.user.strategy;

import com.shuzimali.user.entity.User;

public class SuperAdminStrategy implements UserUpdateStrategy {
    @Override
    public boolean canUpdate(User user, Long currentId) {
        return true;
    }

    @Override
    public void handlePermission(User user) {
        // 超级管理员可能不需要特殊处理
    }
}