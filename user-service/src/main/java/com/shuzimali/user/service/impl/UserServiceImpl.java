package com.shuzimali.user.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.shuzimali.api.client.PermissionClient;
import com.shuzimali.common.exception.BusinessException;
import com.shuzimali.common.exception.ErrorCode;
import com.shuzimali.user.config.JwtProperties;
import com.shuzimali.user.entity.*;

import com.shuzimali.user.mapper.UserMapper;
import com.shuzimali.user.service.TransactionLogsService;
import com.shuzimali.user.service.UserService;
import com.shuzimali.user.strategy.UserUpdateStrategy;
import com.shuzimali.user.strategy.UserUpdateStrategyFactory;
import com.shuzimali.user.utils.JwtTool;
import com.shuzimali.user.utils.RabbitMqHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final PermissionClient permissionClient;
    private final JwtTool jwtTool;
    private final JwtProperties jwtProperties;
    private final RabbitMqHelper rabbitMqHelper;
    private final RocketMQTemplate rocketMQTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final TransactionLogsService transactionLogsService;
    public static final String Topic = "BIND_TOPIC";
    private static final String Tag = "userId";

    @Override
    @Transactional
    public boolean register(UserDTO userDTO) throws UnknownHostException {
        log.info("【用户注册】username={}", userDTO.getUsername());
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
        user.setUserId(IdUtil.getSnowflakeNextId());
        user.setPassword(encryptPassword);
        //permissionClient.bindDefaultRole(user.getUserId());
        String transactionId = UUID.randomUUID().toString().replace("-", "");
        log.info("【发送半消息】transactionId={}", transactionId);

        try {
            if (transactionId.isEmpty()) {
                throw new IllegalArgumentException("transactionId 不能为空");
            }
            Message<?> message = MessageBuilder.withPayload(user)
                    .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                    .build();
            TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(
                    Topic+":"+Tag, message, user);
            log.debug("【发送半消息】sendResult={}", JSON.toJSONString(sendResult));
            if (sendResult == null || !SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                log.error("【发送半消息失败】sendResult={}", JSON.toJSONString(sendResult));
                throw new RuntimeException("事务消息发送失败: " + sendResult);
            }

            } catch (MessagingException e) {
                log.error("【发送半消息异常】", e);
                throw new MessagingException("消息发送过程中发生异常", e);
            }


        Event event = new Event();
        event.setUserId(user.getUserId());
        event.setAction("register_user");
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        event.setIp(ipAddress);
        event.setDetail(user.getUsername()+"用户注册"+ LocalDateTime.now());
        try {
            rabbitMqHelper.sendMessageWithConfirm("exchange.log", "operation.log", event,3);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送操作日志消息到MQ失败");
        }
        return true;
    }

    @Override
    public String login(LoginDTO loginDTO) throws UnknownHostException {
        log.info("【用户登录】username={}", loginDTO.getUsername());
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
        if (user == null) {
            log.info("user login failed, username cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        Event event = new Event();
        event.setUserId(user.getUserId());
        event.setAction("login_user");
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        event.setIp(ipAddress);
        event.setDetail(user.getUsername()+"登录"+ LocalDateTime.now());
        try {
            rabbitMqHelper.sendMessageWithConfirm("exchange.log", "operation.log", event,3);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送操作日志消息到MQ失败");
        }
        return jwtTool.createToken(user.getUserId(), jwtProperties.getTokenTTL());
    }

    @Override
    public List<User> getUsers(Long userId) throws UnknownHostException {
        log.info("【用户查找】userId={}", userId);
        Event event = new Event();
        event.setUserId(userId);
        event.setAction("getUsers_user");
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        event.setIp(ipAddress);
        event.setDetail(userId+"用户查找"+ LocalDateTime.now());
        try {
            rabbitMqHelper.sendMessageWithConfirm("exchange.log", "operation.log", event,3);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送操作日志消息到MQ失败");
        }
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
    public Page<User> getPageUsers(Long userId, int pageNum, int pageSize) throws UnknownHostException {
        log.info("【用户分页查找】userId={},pageNum={},pageSize={}", userId, pageNum, pageSize);
        Event event = new Event();
        event.setUserId(userId);
        event.setAction("getPageUsers_user");
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        event.setIp(ipAddress);
        event.setDetail(userId+"用户分页查找"+ LocalDateTime.now());
        try {
            rabbitMqHelper.sendMessageWithConfirm("exchange.log", "operation.log", event,3);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送操作日志消息到MQ失败");
        }
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
    public User getUserInfo(Long userId, Long id) throws UnknownHostException {
        log.info("【用户查找信息】userId={},id={}", userId, id);
        Event event = new Event();
        event.setUserId(userId);
        event.setAction("getUserInfo_user");
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        event.setIp(ipAddress);
        event.setDetail(userId+"用户查找信息"+ LocalDateTime.now());
        try {
            rabbitMqHelper.sendMessageWithConfirm("exchange.log", "operation.log", event,3);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送操作日志消息到MQ失败");
        }
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
    public Boolean updateUserInfo(Long userId, Long currentId, UserInfo userInfo) throws UnknownHostException {
        log.info("【用户更新信息】userId={},currentId={},userInfo={}", userId, currentId, userInfo);
        Event event = new Event();
        event.setUserId(userId);
        event.setAction("updateUserInfo_user");
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        event.setIp(ipAddress);
        event.setDetail(userId+"用户更新"+ LocalDateTime.now());
        try {
            rabbitMqHelper.sendMessageWithConfirm("exchange.log", "operation.log", event,3);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送操作日志消息到MQ失败");
        }
        String userRoleCode = permissionClient.getUserRoleCode(currentId);
        User user = new User();
        BeanUtils.copyProperties(userInfo, user);
        user.setUserId(userId);
        UserUpdateStrategyFactory strategyFactory = new UserUpdateStrategyFactory(permissionClient);
        UserUpdateStrategy strategy = strategyFactory.getStrategy(userRoleCode);
        if (strategy.canUpdate(user, currentId)) {
            strategy.handlePermission(user);
            return updateById(user);
        }
        return null;
    }

    @Override
    public Boolean updateUserPassword(Long userId, Long currentId, PasswordDTO passwordDTO) throws UnknownHostException {
        log.info("【用户更新密码】userId={},currentId={},passwordDTO={}", userId, currentId, passwordDTO);
        Event event = new Event();
        event.setUserId(userId);
        event.setAction("updateUserPassword_user");
        String ipAddress = InetAddress.getLocalHost().getHostAddress();
        event.setIp(ipAddress);
        event.setDetail(userId+"用户更新密码"+ LocalDateTime.now());
        try {
            rabbitMqHelper.sendMessageWithConfirm("exchange.log", "operation.log", event,3);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发送操作日志消息到MQ失败");
        }
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
        UserUpdateStrategyFactory strategyFactory = new UserUpdateStrategyFactory(permissionClient);
        UserUpdateStrategy strategy = strategyFactory.getStrategy(userRoleCode);
        if (strategy.canUpdate(user, currentId)) {
            strategy.handlePermission(user);
            return updateById(user);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserWithLog(User user,String transactionId) {
        log.info("【用户保存信息】user={},transactionId={}", user, transactionId);
        save(user);
        TransactionLogs transactionLogs = new TransactionLogs();
        transactionLogs.setTransactionId(transactionId);
        transactionLogs.setUserId(user.getUserId());
        transactionLogs.setStatus(0);
        transactionLogsService.save(transactionLogs);
        stringRedisTemplate.opsForSet().add("user:permission:processingBindUserRole", String.valueOf(user.getUserId()));
    }

    @Override
    public void processedCallback(String transactionId) {
        stringRedisTemplate.opsForSet().remove("processingBindUserRole",transactionId);
    }

    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "tanxin";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}
