package com.shuzimali.logging.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.client.Channel;
import com.shuzimali.logging.domain.Event;
import com.shuzimali.logging.domain.OperationLogs;
import com.shuzimali.logging.service.OperationLogsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogsListener {
    private final StringRedisTemplate stringRedisTemplate;
    private final OperationLogsService operationLogsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "operation-logs-queue"
                    , durable = "true"),
            exchange = @Exchange(value = "exchange.log", durable = "true", type = ExchangeTypes.TOPIC),
            key = "operation.log"
    )
            ,ackMode = "MANUAL" )
    public void onApplicationEvent(@Payload Event event
            , Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag
            ,@Header(AmqpHeaders.MESSAGE_ID) String messageId) throws IOException {
        log.info("OperationLogsListener: {}", event);
        try {
            //  幂等检查（防止重复消费）
            String redisKey = "log:messageId:" + messageId;
            Boolean isFirstProcess  = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofMinutes(10));
            if (Boolean.FALSE.equals(isFirstProcess)) {
                log.warn("消息已消费，直接ACK. messageId={}", event.getMessageId());
                channel.basicAck(tag, false);
                return;
            }

            OperationLogs operationLog = new OperationLogs();
            operationLog.setMessageId(event.getMessageId());
            operationLog.setAction(event.getAction());
            operationLog.setUserId(event.getUserId());
            operationLog.setIp(event.getIp());
            operationLog.setDetail(event.getDetail());
            operationLogsService.save(operationLog);

            channel.basicAck(tag, false);
            log.info("日志处理完成: {}", event);

        } catch (DuplicateKeyException e) {
            log.warn("数据库幂等冲突: {}", e.getMessage());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("其它处理异常: {}", e.getMessage(), e);
            channel.basicNack(tag, false, true);
        }
    }
}

