package com.shuzimali.permission.controller;

import com.shuzimali.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;
    @PutMapping("/bindDefaultRole")
    public void bindDefaultRole(@RequestParam("userId") Long userId){
        permissionService.bindDefaultRole(userId);
    }

    // 查询用户角色码（返回role_code）
    @GetMapping("/getUserRoleCode/{userId}")
    public String getUserRoleCode(@PathVariable("userId") Long userId){
        return permissionService.getUserRoleCode(userId);
    }

    // 超管调用：升级用户为管理员
    @PutMapping("/upgradeToAdmin/{userId}")
    public void upgradeToAdmin(@PathVariable("userId") Long userId){
        permissionService.upgradeToAdmin(userId);
    }

    // 超管调用：降级用户为普通角色
    @PutMapping("/downgradeToUser/{userId}")
    public void downgradeToUser(@PathVariable("userId") Long userId){
        permissionService.downgradeToUser(userId);
    }
    @GetMapping("/getNormalUsers")
    public List<Long> getNormalUsers(){
        return permissionService.getNormalUsers();
    }

}
