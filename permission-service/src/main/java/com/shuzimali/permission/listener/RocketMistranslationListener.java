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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


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
    private final RedissonClient redissonClient;



    @Override
    public void onMessage(User user) {
       try {
            RLock lock = redissonClient.getLock(String.format("lock:permission:%d", user.getUserId()));
            boolean isLock = lock.tryLock(10, TimeUnit.MINUTES);
            if (!isLock) {
                throw new RuntimeException("重复消费");
            }
            try {
                log.info("消费消息 事务消息：{}", user);
                UserRole userRole = new UserRole();
                userRole.setUserId(user.getUserId());

                Roles roles = rolesService.lambdaQuery().eq(Roles::getRoleCode, "user").one();
                if (roles == null) {
                    throw new RuntimeException("角色 'user' 不存在");
                }
                userRole.setRoleId(roles.getRoleId());
                permissionService.save(userRole);
            } catch (Exception e) {
                log.error("处理用户权限绑定时发生异常", e);
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        stringRedisTemplate.opsForSet().remove("user:permission:processingBindUserRole", String.valueOf(user.getUserId()));

    }
}
