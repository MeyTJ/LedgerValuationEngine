package com.ledger.valuation.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.valuation.domain.DomainEvent;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
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
                DomainEvent.AccountValued.class,
                PortfolioLedgerEvent.PortfolioAccountOpened.class,
                PortfolioLedgerEvent.TransactionCommitted.class,
                PortfolioLedgerEvent.FeeAccrued.class,
                PortfolioLedgerEvent.InterestCredited.class,
                PortfolioLedgerEvent.AccountValueAdjustmentPosted.class,
                PortfolioLedgerEvent.MarkToMarketApplied.class,
                PortfolioLedgerEvent.FxRateCommitted.class,
                PortfolioLedgerEvent.AccountValueSnapshot.class,
                PortfolioLedgerEvent.PortfolioStatusChanged.class,
                PortfolioLedgerEvent.AccrualPosted.class,
                PortfolioLedgerEvent.PositionOpened.class,
                PortfolioLedgerEvent.PolicyEvaluated.class
        );
        return mapper;
    }
}
