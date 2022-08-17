package com.webank.webase.front.base.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web 配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    /**
     * 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 前端目录
        registry.addResourceHandler(
            "/static/,classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/");
    }
}

