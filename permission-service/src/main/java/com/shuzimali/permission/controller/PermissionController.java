package com.shuzimali.permission.controller;

import com.shuzimali.permission.service.PermissionService;
import com.shuzimali.permission.service.impl.UserRoleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;
    @PostMapping("/permission/bindDefaultRole")
    public void bindDefaultRole(@RequestParam("id") Long userId){
        permissionService.bindDefaultRole(userId);
    }
}
