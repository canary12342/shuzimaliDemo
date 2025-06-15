package com.shuzimali.user.config;

import cn.hutool.core.collection.CollUtil;

import com.shuzimali.user.interceptors.LoginInterceptor;
import com.shuzimali.user.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

   private final JwtTool jwtTool;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1.添加拦截器
        LoginInterceptor loginInterceptor = new LoginInterceptor(jwtTool);
        InterceptorRegistration registration = registry.addInterceptor(loginInterceptor);
        registration.excludePathPatterns(
                "/user/login",
                "/user/register",
                "/error",
                "/favicon.ico",
                "/v2/**",
                "/v3/**",
                "/swagger-resources/**",
                "/webjars/**",
                "/doc.html"
        );
    }
}
