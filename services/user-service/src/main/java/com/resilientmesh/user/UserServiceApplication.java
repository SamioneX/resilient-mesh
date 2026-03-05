package com.resilientmesh.user;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    TaskExecutor applicationTaskExecutor(TaskExecutorBuilder ignored) {
        ExecutorService virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor();
        return new ConcurrentTaskExecutor(virtualThreadPool);
    }
}
