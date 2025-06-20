package com.shuzimali.user.scheduled;

import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import com.shuzimali.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ScheduledCheak implements CommandLineRunner {
    private final StringRedisTemplate stringRedisTemplate;
    private final UserService userService;
    @Override
    public void run(String... args) {
        CronUtil.schedule("*/30 * * * * *", (Task) () -> {
            Set<String> members = stringRedisTemplate.opsForSet().members("user:permission:processingBindUserRole");
            if (members != null) {
                for(String userId:members){
                    userService.removeById(Long.valueOf(userId));
                }
                stringRedisTemplate.delete("user:permission:processingBindUserRole");
            }
        });
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
}