package com.ledger.valuation.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.domain.DomainEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.build();
        mapper.registerSubtypes(
                DomainEvent.AccountOpened.class,
                DomainEvent.TransactionPosted.class,
                DomainEvent.AccountValued.class
        );
        return mapper;
    }
}
