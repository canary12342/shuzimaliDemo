package com.shuzimali.user.strategy;

import com.shuzimali.api.client.PermissionClient;
import com.shuzimali.user.entity.User;

import java.util.List;

public class AdminUserStrategy implements UserUpdateStrategy {
    private PermissionClient permissionClient;

    public AdminUserStrategy(PermissionClient permissionClient) {
        this.permissionClient = permissionClient;
    }

    @Override
    public boolean canUpdate(User user, Long currentId) {
        List<Long> userIds = permissionClient.getNormalUsers();
        return userIds.contains(user.getUserId())||user.getUserId().equals(currentId);
    }

    @Override
    public void handlePermission(User user) {
        // permissionClient.upgradeToAdmin(user.getId());
    }
}