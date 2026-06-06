package com.ledger.valuation.infrastructure.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ledger.valuation.application.readmodel.AccountValueDashboardView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, AccountValueDashboardView> accountCodeDashboardIndex(
            @Value("${ledger.readside.cache.max-entries}") long maxEntries
    ) {
        return Caffeine.newBuilder()
                .maximumSize(maxEntries)
                .build();
    }

    @Bean
    public Cache<UUID, AccountValueDashboardView> portfolioDashboardIndex(
            @Value("${ledger.readside.cache.max-entries}") long maxEntries
    ) {
        return Caffeine.newBuilder()
                .maximumSize(maxEntries)
                .build();
    }

    @Bean
    public Cache<String, Long> accountValueCache(
            @Value("${ledger.readside.cache.max-entries}") long maxEntries,
            @Value("${ledger.readside.cache.expire-after-write}") Duration expireAfterWrite
    ) {
        return Caffeine.newBuilder()
                .maximumSize(maxEntries)
                .expireAfterWrite(expireAfterWrite)
                .build();
    }
}
