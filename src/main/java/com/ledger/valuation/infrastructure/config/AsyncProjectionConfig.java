package com.ledger.valuation.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncProjectionConfig {

    @Bean(name = "ledgerProjectionExecutor")
    public Executor ledgerProjectionExecutor() {
        return Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("lve-projection-", 0).factory()
        );
    }
}
