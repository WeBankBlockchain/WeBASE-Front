//package com.webank.webase.front.base.config;
//
//import static com.webank.webase.front.base.properties.Constants.SOLC_DIR_PATH_CONFIG;
//
//import java.io.File;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//
//@Slf4j
//@Configuration
//public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // File fileDir = new File(SOLC_DIR_PATH);
//        // String path = fileDir.getAbsolutePath();
//        // registry.addResourceHandler("/solcjs/**").addResourceLocations("file:/" + path + File.separator);
//
//        // web request uri of "/solcjs/**", return the files in /solcjs/xx.js
//        registry.addResourceHandler("/solcjs/**").addResourceLocations("classpath:/" + SOLC_DIR_PATH_CONFIG + File.separator);
//        super.addResourceHandlers(registry);
//    }
//}