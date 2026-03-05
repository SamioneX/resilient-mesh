package com.resilientmesh.wallet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

@SpringBootApplication
public class WalletServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }

    @Bean
    TaskExecutor applicationTaskExecutor() {
        ExecutorService virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor();
        return new ConcurrentTaskExecutor(virtualThreadPool);
    }
}
