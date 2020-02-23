package com.madhu.IssueSearch.configuration;

import com.madhu.IssueSearch.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ExecutorConfiguration {

    @Autowired
    private AppConfig config;

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getThreadPool());
        executor.setMaxPoolSize(config.getThreadPool());
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("GithubThread-");
        executor.initialize();
        return executor;
    }
}
