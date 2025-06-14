package com.shuzimali.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuzimali.common.exception.BusinessException;
import com.shuzimali.common.exception.ErrorCode;
import com.shuzimali.entity.User;
import com.shuzimali.entity.UserDTO;

import com.shuzimali.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public boolean register(UserDTO userDTO) {
        List<User> list = lambdaQuery().eq(User::getUsername, userDTO.getUsername()).list();

        if (!list.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        String encryptPassword = getEncryptPassword(userDTO.getPassword());

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        //todo
        permissonClient.addUserPermission(user.getUserId());
        //todo MQ发送消息到日志微服务

        return true;
    }

    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "tanxin";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}
