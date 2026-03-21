package org.example.work.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 包装 SaInterceptor，跳过 SSE 异步 dispatch（此时 SaToken 上下文不可用）
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()) {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // 异步 dispatch 时 DispatcherType 为 ASYNC，跳过鉴权
                if (request.getDispatcherType() == jakarta.servlet.DispatcherType.ASYNC) {
                    return true;
                }
                return super.preHandle(request, response, handler);
            }
        })
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
}
