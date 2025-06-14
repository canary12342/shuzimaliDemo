package com.shuzimali.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shuzimali.user.entity.LoginDTO;
import com.shuzimali.user.entity.User;
import com.shuzimali.user.entity.UserDTO;

public interface UserService extends IService<User> {
    boolean register(UserDTO userDTO);

    String login(LoginDTO loginDTO);
}
