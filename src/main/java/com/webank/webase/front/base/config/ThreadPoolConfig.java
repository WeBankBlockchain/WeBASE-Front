package com.webank.webase.front.base.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步执行liquid编译
 **/
@Configuration
@EnableScheduling
@Log4j2
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolTaskScheduler compileAsyncScheduler() {
        log.info("start compileAsyncScheduler init...");
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.afterPropertiesSet();
        scheduler.setThreadNamePrefix("ThreadPoolTaskScheduler-async-compile:");
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return scheduler;
    }
}
