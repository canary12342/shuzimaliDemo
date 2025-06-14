package com.shuzimali.user.controller;

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
    public Result<List<User>> getUsers(){
        return ResultUtils.success(userService.list());
    }

    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId,
                                    @RequestHeader("Authorization") String token){
        return null;
    }
}