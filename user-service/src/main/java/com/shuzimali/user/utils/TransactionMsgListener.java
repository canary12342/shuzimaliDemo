package com.shuzimali.user.utils;

import com.shuzimali.user.entity.TransactionLogs;
import com.shuzimali.user.entity.User;
import com.shuzimali.user.service.TransactionLogsService;
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
    private final TransactionLogsService transactionLogsService;

    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        MessageHeaders messageHeaders = message.getHeaders();
        String transactionId = (String) messageHeaders.get(RocketMQHeaders.TRANSACTION_ID);
        log.info("【执行本地事务】消息体参数：transactionId={}", transactionId);

        try {
            User user = (User) o;
            userService.saveUserWithLog(user, transactionId);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.error("【执行本地事务】发生异常，消息将被回滚", e);
            return RocketMQLocalTransactionState.ROLLBACK;
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
        TransactionLogs transactionLogs;
        try {
            transactionLogs = transactionLogsService.lambdaQuery().eq(TransactionLogs::getTransactionId, transactionId).list().get(0);
        } catch (Exception e) {
            log.error("【回查本地事务】transactionId={} 查询 mysql 异常", transactionId, e);
            return RocketMQLocalTransactionState.UNKNOWN;
        }

        if (transactionLogs == null) {
            log.warn("【回查本地事务】transactionId={} 未在 mysql 中找到记录", transactionId);
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        log.info("【回查本地事务】transactionId={} 查询成功，值为: {}", transactionId, transactionLogs);
        return RocketMQLocalTransactionState.COMMIT;
    }

}

