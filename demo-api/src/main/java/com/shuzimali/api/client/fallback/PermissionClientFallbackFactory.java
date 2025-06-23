package com.shuzimali.api.client.fallback;


import com.shuzimali.api.client.PermissionClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

@Slf4j
public class PermissionClientFallbackFactory implements FallbackFactory<PermissionClient> {
    @Override
    public PermissionClient create(Throwable cause) {
        return new PermissionClient() {
            @Override
            public void bindDefaultRole(Long userId) {
                throw new RuntimeException("服务调用失败，未实现降级逻辑", cause);
            }

            @Override
            public String getUserRoleCode(Long userId) {
                log.error("获取用户角色失败", cause);
                return "user";
            }

            @Override
            public void upgradeToAdmin(Long userId) {
                throw new RuntimeException("服务调用失败，未实现降级逻辑", cause);
            }

            @Override
            public void downgradeToUser(Long userId) {
                throw new RuntimeException("服务调用失败，未实现降级逻辑", cause);
            }

            @Override
            public List<Long> getNormalUsers() {
                throw new RuntimeException("服务调用失败，未实现降级逻辑", cause);
            }
        };
    }
}
