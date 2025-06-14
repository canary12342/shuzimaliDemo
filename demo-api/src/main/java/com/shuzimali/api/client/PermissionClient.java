package com.shuzimali.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("shuzimali-permission-service")
public interface PermissionClient {
    // 绑定默认角色（普通用户）
    @PutMapping("/permission/bindDefaultRole")
    void bindDefaultRole(@RequestParam("userId") Long userId);

    // 查询用户角色码（返回role_code）
    @GetMapping("/permission/getUserRoleCode/{userId}")
    String getUserRoleCode(@PathVariable("userId") Long userId);

    // 超管调用：升级用户为管理员
    @PutMapping("/permission/upgradeToAdmin/{userId}")
    void upgradeToAdmin(@PathVariable("userId") Long userId);

    // 超管调用：降级用户为普通角色
    @PutMapping("/permission/downgradeToUser/{userId}")
    void downgradeToUser(@PathVariable("userId") Long userId);

    @GetMapping("/permission/getNormalUsers")
    List<Long> getNormalUsers();
}
