package com.shuzimali.api.config;


import com.shuzimali.api.client.fallback.PermissionClientFallbackFactory;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level fullFeignLoggerLevel(){
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {

            }
        };
    }
    @Bean
    public PermissionClientFallbackFactory permissionClientFallbackFactory(){
        return new PermissionClientFallbackFactory();
    }

}
