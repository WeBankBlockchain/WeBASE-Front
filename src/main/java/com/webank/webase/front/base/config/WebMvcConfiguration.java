package com.webank.webase.front.base.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Slf4j
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // web request uri of "/solcjs/**", return the files in classpath/solcjs/xx.js
        registry.addResourceHandler("/solcjs/**").addResourceLocations("classpath:/solcjs/");
        super.addResourceHandlers(registry);
    }
}