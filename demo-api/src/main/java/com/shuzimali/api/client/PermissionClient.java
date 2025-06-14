package com.shuzimali.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("shuzimali-permission-service")
public interface PermissionClient {
    // 绑定默认角色（普通用户）
    @PostMapping("/permission/bindDefaultRole")
    void bindDefaultRole(@RequestParam("id") Long userId);

    // 查询用户角色码（返回role_code）
    String getUserRoleCode(Long userId);

    // 超管调用：升级用户为管理员
    void upgradeToAdmin(Long userId);

    // 超管调用：降级用户为普通角色
    void downgradeToUser(Long userId);
}
