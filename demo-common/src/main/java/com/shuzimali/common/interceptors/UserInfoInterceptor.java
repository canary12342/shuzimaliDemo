package com.shuzimali.common.interceptors;

import cn.hutool.core.util.StrUtil;
import com.shuzimali.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInfoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取登录用户信息
        String token = request.getHeader("user-info");
        // 2.判断是否获取了用户，如果有，存入ThreadLocal
        if (StrUtil.isNotBlank(token)) {
            //todo  从token中获取用户
            UserContext.setUser(Long.valueOf(token));
        }
        // 3.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户
        UserContext.removeUser();
    }
}
