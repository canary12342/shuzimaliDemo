package com.shuzimali.user.strategy;

import com.shuzimali.user.entity.User;

public class NormalUserStrategy implements UserUpdateStrategy {
    @Override
    public boolean canUpdate(User user, Long currentId) {
        return user.getUserId().equals(currentId);
    }

    @Override
    public void handlePermission(User user) {
        // permissionClient.downgradeToUser(user.getId());
    }
}