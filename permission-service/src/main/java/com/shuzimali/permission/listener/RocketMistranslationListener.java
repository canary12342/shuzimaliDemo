package com.shuzimali.permission.listener;

import com.alibaba.fastjson.JSON;
import com.shuzimali.permission.domain.Roles;
import com.shuzimali.permission.domain.User;
import com.shuzimali.permission.domain.UserRole;
import com.shuzimali.permission.service.PermissionService;
import com.shuzimali.permission.service.RolesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 消费事务消息
 * 配置RocketMQ监听
 * @author qzz
 */
@Service
@Slf4j
@RequiredArgsConstructor
@RocketMQMessageListener(consumerGroup = "Tx_Charge_Group"
        ,topic = "BIND_TOPIC"
        ,messageModel = MessageModel.CLUSTERING
        ,selectorExpression = "userId")
public class RocketMistranslationListener implements RocketMQListener<User> {
    private final RolesService rolesService;
    private final PermissionService permissionService;
    private final StringRedisTemplate stringRedisTemplate;



    @Override
    public void onMessage(User user) {
        try {
            stringRedisTemplate.opsForValue().setIfAbsent(String.valueOf(user.getUserId()), "1", Duration.ofMinutes(10));
            log.info("消费消息 事务消息：{}", user);
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getUserId());
            Roles roles = rolesService.lambdaQuery().eq(Roles::getRoleCode, "user").one();
            userRole.setRoleId(roles.getRoleId());
            permissionService.save(userRole);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //userClient.processedCallback(transactionId);
        stringRedisTemplate.opsForSet().remove("user:permission:processingBindUserRole",String.valueOf(user.getUserId()));
    }
}
