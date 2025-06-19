package com.shuzimali.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shuzimali.user.entity.*;

import java.net.UnknownHostException;
import java.util.List;

public interface UserService extends IService<User> {
    boolean register(UserDTO userDTO) throws UnknownHostException;

    String login(LoginDTO loginDTO) throws UnknownHostException;

    List<User> getUsers(Long userId) throws UnknownHostException;

    Page<User> getPageUsers(Long userId, int pageNum, int pageSize) throws UnknownHostException;

    User getUserInfo(Long userId, Long id) throws UnknownHostException;

    Boolean updateUserInfo(Long userId, Long currentId, UserInfo userInfo) throws UnknownHostException;

    Boolean updateUserPassword(Long userId, Long currentId, PasswordDTO passwordDTO) throws UnknownHostException;

    void saveUserWithLog(User user,String transactionId);

    void processedCallback(String transactionId);
}
