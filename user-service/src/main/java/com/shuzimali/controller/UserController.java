package com.shuzimali.controller;

import com.shuzimali.entity.LoginDTO;
import com.shuzimali.entity.Result;
import com.shuzimali.entity.User;
import com.shuzimali.entity.UserDTO;
import com.shuzimali.service.UserService;
import com.shuzimali.utils.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        return null;
    }

    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId,
                                    @RequestHeader("Authorization") String token){
        return null;
    }
}