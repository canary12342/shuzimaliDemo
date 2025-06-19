package com.shuzimali.user.utils;

import com.shuzimali.user.entity.User;
import com.shuzimali.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.checkerframework.checker.index.qual.SameLen;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@RocketMQTransactionListener
public class TransactionMsgListener implements RocketMQLocalTransactionListener {
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        MessageHeaders messageHeaders = message.getHeaders();
        String transactionId = (String) messageHeaders.get(RocketMQHeaders.TRANSACTION_ID);
        log.info("【执行本地事务】消息体参数：transactionId={}", transactionId);

        // 执行带有事务注解的本地方法：
        try {
            User user = (User) o;
            userService.saveUserWithLog(user, transactionId);
            return RocketMQLocalTransactionState.COMMIT; // 正常：向MQ Server发送commit消息
        } catch (Exception e) {
            log.error("【执行本地事务】发生异常，消息将被回滚", e);
            return RocketMQLocalTransactionState.ROLLBACK; // 异常：向MQ Server发送rollback消息
        }

    }

    /**
     * 检查本地事务的状态
     *
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        MessageHeaders headers = message.getHeaders();
        String transactionId = headers.get(RocketMQHeaders.TRANSACTION_ID, String.class);
        if (transactionId == null) {
            log.warn("【回查本地事务】transactionId 为空");
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        log.info("【回查本地事务】transactionId={}, 开始查询", transactionId);
        String s;
        try {
            s = stringRedisTemplate.opsForValue().get(transactionId);
        } catch (Exception e) {
            log.error("【回查本地事务】transactionId={} 查询 Redis 异常", transactionId, e);
            return RocketMQLocalTransactionState.UNKNOWN; // 触发 RocketMQ 重试
        }

        if (s == null) {
            log.warn("【回查本地事务】transactionId={} 未在 Redis 中找到记录", transactionId);
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        if (s.isEmpty()) {
            log.warn("【回查本地事务】transactionId={} 在 Redis 中为空字符串", transactionId);
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        log.info("【回查本地事务】transactionId={} 查询成功，值为: {}", transactionId, s);
        return RocketMQLocalTransactionState.COMMIT;
    }

}

