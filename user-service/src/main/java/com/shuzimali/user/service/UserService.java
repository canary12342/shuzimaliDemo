package com.shuzimali.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shuzimali.user.entity.*;

import java.util.List;

public interface UserService extends IService<User> {
    boolean register(UserDTO userDTO);

    String login(LoginDTO loginDTO);

    List<User> getUsers(Long userId);

    Page<User> getPageUsers(Long userId, int pageNum, int pageSize);

    User getUserInfo(Long userId, Long id);

    Boolean updateUserInfo(Long userId, Long currentId, UserInfo userInfo);

    Boolean updateUserPassword(Long userId, Long currentId, PasswordDTO passwordDTO);
}
