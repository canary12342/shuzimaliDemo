package com.shuzimali.user;

import com.shuzimali.api.config.DefaultFeignConfig;
import com.shuzimali.user.controller.Producer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@MapperScan("com.shuzimali.user.mapper")
@EnableFeignClients(basePackages = "com.shuzimali.api.client", defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
       SpringApplication.run(UserServiceApplication.class, args);
    }
}