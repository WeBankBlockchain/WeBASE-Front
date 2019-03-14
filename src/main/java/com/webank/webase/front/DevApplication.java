package com.webank.webase.front;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Startup class
 * 
 * @author v_sflkchen
 *
 */
@Slf4j
@EnableSwagger2
@SpringBootApplication
@EnableScheduling
public class DevApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(DevApplication.class, args);
        log.info("main run success...");
    }
}
