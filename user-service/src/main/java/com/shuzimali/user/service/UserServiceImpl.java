package com.shuzimali.user.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.shuzimali.api.client.PermissionClient;
import com.shuzimali.common.exception.BusinessException;
import com.shuzimali.common.exception.ErrorCode;
import com.shuzimali.user.config.JwtProperties;
import com.shuzimali.user.entity.*;

import com.shuzimali.user.mapper.UserMapper;
import com.shuzimali.user.utils.JwtTool;
import com.shuzimali.user.utils.RabbitMqHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final PermissionClient permissionClient;
    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;
    private final RabbitMqHelper rabbitMqHelper;

    @Override
    @Transactional
    public boolean register(UserDTO userDTO) throws UnknownHostException {
        List<User> list = lambdaQuery().eq(User::getUsername, userDTO.getUsername()).list();
        String username = userDTO.getUsername();
        String userPassword = userDTO.getPassword();
        String checkPassword = userDTO.getCheckPassword();
        if (!list.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        String encryptPassword = getEncryptPassword(userDTO.getPassword());

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        permissionClient.bindDefaultRole(user.getUserId());
        Event event = new Event();
        event.setUserId(user.getUserId());
        event.setAction("register_user");
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        event.setIp(ipAddress);
        event.setDetail("用户注册成功"+ LocalDateTime.now());
        try {
            rabbitMqHelper.sendMessageWithConfirm("exchange.log", "operation.log", event,3);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送操作日志消息到MQ失败");
        }
        return true;
    }

    @Override
    public String login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String userPassword = loginDTO.getPassword();
        // 1. 校验
        if (StrUtil.hasBlank(username, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        // 2. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询数据库中的用户是否存在
        User user = lambdaQuery().eq(User::getUsername, username).eq(User::getPassword, encryptPassword).one();
        // 不存在，抛异常
        if (user == null) {
            log.info("user login failed, username cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        return jwtTool.createToken(user.getUserId(), jwtProperties.getTokenTTL());
    }

    @Override
    public List<User> getUsers(Long userId) {
        String userRoleCode = permissionClient.getUserRoleCode(userId);
        if ("user".equals(userRoleCode)){
            return List.of(getById(userId));
        }else if("admin".equals(userRoleCode)){
            List<Long> userIds =permissionClient.getNormalUsers();
            return lambdaQuery().in(User::getUserId, userIds).list();
        }else {
            return lambdaQuery().list();
        }
    }

    @Override
    public Page<User> getPageUsers(Long userId, int pageNum, int pageSize) {
        String userRoleCode = permissionClient.getUserRoleCode(userId);
        Page<User> page = new Page<>(pageNum, pageSize);
        if ("user".equals(userRoleCode)){
            return lambdaQuery().eq(User::getUserId, userId).page(page);
        }else if("admin".equals(userRoleCode)){
            List<Long> userIds =permissionClient.getNormalUsers();
            return lambdaQuery().in(User::getUserId, userIds).page(page);
        }else {
            return lambdaQuery().page(page);
        }
    }

    @Override
    public User getUserInfo(Long userId, Long id) {
        String userRoleCode = permissionClient.getUserRoleCode(id);
        if ("user".equals(userRoleCode)){
            return userId.equals(id) ?getById(userId):null;
        }else if("admin".equals(userRoleCode)){
            List<Long> userIds =permissionClient.getNormalUsers();
            return userIds.contains(userId)?getById(userId):null;
        }else {
            return getById(userId);
        }
    }

    @Override
    public Boolean updateUserInfo(Long userId, Long currentId, UserInfo userInfo) {
        String userRoleCode = permissionClient.getUserRoleCode(currentId);
        User user = new User();
        BeanUtils.copyProperties(userInfo, user);
        user.setUserId(userId);
        if ("user".equals(userRoleCode)){
            return userId.equals(currentId) ?updateById(user):null;
        }else if("admin".equals(userRoleCode)){
            List<Long> userIds =permissionClient.getNormalUsers();
            return userIds.contains(userId)?updateById(user):null;
        }else {
            return updateById(user);
        }
    }

    @Override
    public Boolean updateUserPassword(Long userId, Long currentId, PasswordDTO passwordDTO) {
        String userRoleCode = permissionClient.getUserRoleCode(currentId);
        String userPassword = passwordDTO.getOldPassword();
        String newPassword = passwordDTO.getNewPassword();
        // 1. 校验
        if (StrUtil.hasBlank(userPassword,newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userPassword.length() < 8|| newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        // 2. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        String newEncryptPassword = getEncryptPassword(newPassword);
        // 3. 查询数据库中的用户是否存在
        User user = lambdaQuery().eq(User::getUserId, userId).eq(User::getPassword, encryptPassword).one();
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        user.setPassword(newEncryptPassword);
        if ("user".equals(userRoleCode)){
            return userId.equals(currentId) ?updateById(user):null;
        }else if("admin".equals(userRoleCode)){
            List<Long> userIds =permissionClient.getNormalUsers();
            return userIds.contains(userId)?updateById(user):null;
        }else {
            return updateById(user);
        }
    }

    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "tanxin";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}
