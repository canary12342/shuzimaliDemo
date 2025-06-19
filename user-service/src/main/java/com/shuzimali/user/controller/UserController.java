package com.shuzimali.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzimali.common.utils.UserContext;
import com.shuzimali.user.entity.*;
import com.shuzimali.user.service.UserService;
import com.shuzimali.user.utils.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/addUser")
    public Result<?> addUser(@RequestBody User user){
        return ResultUtils.success(userService.save(user));
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody UserDTO userDTO) throws UnknownHostException {

        return  ResultUtils.success(userService.register(userDTO));
    }


    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO loginDTO) throws UnknownHostException {
        return ResultUtils.success(userService.login(loginDTO));
    }
    @GetMapping("/users")
    public Result<Page<User>> getUsers(@RequestParam Integer pageNum,@RequestParam Integer pageSize) throws UnknownHostException {
        Long userId = UserContext.getUser();
        return ResultUtils.success(userService.getPageUsers(userId, pageNum, pageSize));
    }

    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) throws UnknownHostException {
        Long id = UserContext.getUser();
        return ResultUtils.success(userService.getUserInfo(userId,id));
    }
    @PutMapping("/{userId}")
    public Result<Boolean> updateUserInfo(@PathVariable Long userId,@RequestBody UserInfo userInfo) throws UnknownHostException {
        Long currentId = UserContext.getUser();
        return ResultUtils.success(userService.updateUserInfo(userId,currentId,userInfo));
    }
    @PostMapping("/reset-password")
    public Result<Boolean> updateUserPassword(PasswordDTO passwordDTO) throws UnknownHostException {
        Long currentId = UserContext.getUser();
        Long userId = passwordDTO.getUserId();
        return ResultUtils.success(userService.updateUserPassword(userId,currentId,passwordDTO));
    }
    @GetMapping("/processedCallback/{id}")
    public void processedCallback(@PathVariable String id){
        userService.processedCallback(id);
    }
}