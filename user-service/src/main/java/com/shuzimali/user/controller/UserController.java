package com.shuzimali.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuzimali.common.utils.UserContext;
import com.shuzimali.user.entity.LoginDTO;
import com.shuzimali.user.entity.Result;
import com.shuzimali.user.entity.User;
import com.shuzimali.user.entity.UserDTO;
import com.shuzimali.user.service.UserService;
import com.shuzimali.user.utils.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public Result<?> register(@RequestBody UserDTO userDTO){

        return  ResultUtils.success(userService.register(userDTO));
    }


    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO loginDTO){
        return ResultUtils.success(userService.login(loginDTO));
    }
    @GetMapping("/users")
    public Result<Page<User>> getUsers(@RequestParam Integer pageNum,@RequestParam Integer pageSize){
        Long userId = UserContext.getUser();
        return ResultUtils.success(userService.getPageUsers(userId, pageNum, pageSize));
    }

    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId,
                                    @RequestHeader("Authorization") String token){
        return null;
    }
}