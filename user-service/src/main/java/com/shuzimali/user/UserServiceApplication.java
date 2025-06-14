package com.shuzimali.user;

import com.shuzimali.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.shuzimali.user.mapper")
@EnableFeignClients(basePackages = "com.shuzimali.api.client", defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}