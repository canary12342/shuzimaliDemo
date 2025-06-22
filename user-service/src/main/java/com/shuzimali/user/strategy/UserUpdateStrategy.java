package com.shuzimali.user.strategy;

import com.shuzimali.user.entity.User;

public interface UserUpdateStrategy {
    boolean canUpdate(User user, Long currentId);
    void handlePermission(User user);
}