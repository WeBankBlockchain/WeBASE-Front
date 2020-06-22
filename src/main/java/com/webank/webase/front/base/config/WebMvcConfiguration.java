package com.webank.webase.front.base.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;

import static com.webank.webase.front.solc.SolcService.SOLC_DIR_PATH;


@Slf4j
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // web request uri of "/solcjs/**", return the files in /solcjs/xx.js
        File fileDir = new File(SOLC_DIR_PATH);
        String path = fileDir.getAbsolutePath();
        registry.addResourceHandler("/solcjs/**").addResourceLocations("file:/" + path + File.separator);
        super.addResourceHandlers(registry);
    }
}