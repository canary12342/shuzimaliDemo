package com.shuzimali.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuzimali.entity.User;
import com.shuzimali.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
