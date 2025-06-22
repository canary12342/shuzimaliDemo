package com.shuzimali.user.strategy;

import com.shuzimali.api.client.PermissionClient;

import java.util.HashMap;
import java.util.Map;

public class UserUpdateStrategyFactory {
    private PermissionClient permissionClient;
    
    private Map<String, UserUpdateStrategy> strategies;

    public UserUpdateStrategyFactory(PermissionClient permissionClient) {
        this.permissionClient = permissionClient;
        initStrategies();
    }

    private void initStrategies() {
        strategies = new HashMap<>();
        strategies.put("user", new NormalUserStrategy());
        strategies.put("admin", new AdminUserStrategy(permissionClient));
        strategies.put("superAdmin", new SuperAdminStrategy());
    }

    public UserUpdateStrategy getStrategy(String userRoleCode) {
        return strategies.getOrDefault(userRoleCode, new SuperAdminStrategy());
    }
}