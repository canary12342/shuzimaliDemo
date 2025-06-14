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
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogsListener {
    private final OperationLogsService operationLogsService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "operation-logs-queue"
                    , durable = "true"
                    /*,arguments = {
                        // 设置最大重试次数（例如3次）
                        @Argument(name = "x-max-attempts", value = "3", type = "long"),

                        // 设置消息TTL（可选）
                        @Argument(name = "x-message-ttl", value = "10000", type = "long"),

                        // 设置死信交换机
                        @Argument(name = "x-dead-letter-exchange", value = "exchange.dlq"),

                        // 设置死信路由键（可选）
                        @Argument(name = "x-dead-letter-routing-key", value = "dlq.operation.log")
                    }*/),
            exchange = @Exchange(value = "exchange.log", durable = "true", type = ExchangeTypes.TOPIC),
            key = "operation.log"
    )
            ,ackMode = "MANUAL" )
    public void onApplicationEvent(@Payload Event event
            , Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("OperationLogsListener: {}", event);
        try {

            //  幂等检查（防止重复消费）
            if (operationLogsService.existsByMessageId(event.getMessageId())) {
                log.warn("消息已消费，直接ACK. messageId={}", event.getMessageId());
                channel.basicAck(tag, false); // 确认消息
                return;
            }

            // 业务处理（写入数据库）
            OperationLogs operationLog = new OperationLogs();
            operationLog.setMessageId(event.getMessageId());
            operationLog.setAction(event.getAction());
            operationLog.setUserId(event.getUserId());
            operationLog.setIp(event.getIp());
            operationLog.setDetail(event.getDetail());
            operationLogsService.save(operationLog);

            // 手动ACK确认
            channel.basicAck(tag, false);
            log.info("日志处理完成: {}", event);

        } catch (DuplicateKeyException e) {
            log.warn("数据库幂等冲突: {}", e.getMessage());
            channel.basicAck(tag, false); // 已处理过，直接ACK
        } catch (Exception e) {
            log.error("其它处理异常: {}", e.getMessage(), e);
            //业务异常，NACK并重回队列（延迟重试）
            channel.basicNack(tag, false, true);
        }
    }
}

