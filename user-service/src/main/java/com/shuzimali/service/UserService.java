package com.shuzimali.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shuzimali.entity.User;
import com.shuzimali.entity.UserDTO;
import org.springframework.stereotype.Service;

public interface UserService extends IService<User> {
    boolean register(UserDTO userDTO);
}
