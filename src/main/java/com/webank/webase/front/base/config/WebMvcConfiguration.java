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
        // 前端浏览器访问路径带有/file/**就转到对应磁盘下读取js文件,
        // 类似前端访问tomcat webapp下file文件夹中文件
        File fileDir = new File(SOLC_DIR_PATH);
        // check parent path
        if(!fileDir.exists()){
            // 递归生成文件夹
            fileDir.mkdirs();
        }
        String filePath = fileDir.getAbsolutePath() + File.separator;
        log.info("addResourceHandlers path:{}", filePath);
        registry.addResourceHandler("/file/**").addResourceLocations("file:" + filePath);
        super.addResourceHandlers(registry);
    }
}