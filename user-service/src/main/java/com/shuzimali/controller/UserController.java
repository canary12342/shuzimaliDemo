package com.shuzimali.controller;

import com.shuzimali.entity.LoginDTO;
import com.shuzimali.entity.Result;
import com.shuzimali.entity.User;
import com.shuzimali.entity.UserDTO;
import com.shuzimali.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody UserDTO userDTO){
        return null;
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