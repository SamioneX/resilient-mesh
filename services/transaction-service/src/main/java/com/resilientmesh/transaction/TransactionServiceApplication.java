package com.resilientmesh.transaction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

@SpringBootApplication
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }

    @Bean
    TaskExecutor applicationTaskExecutor() {
        ExecutorService virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor();
        return new ConcurrentTaskExecutor(virtualThreadPool);
    }
}
