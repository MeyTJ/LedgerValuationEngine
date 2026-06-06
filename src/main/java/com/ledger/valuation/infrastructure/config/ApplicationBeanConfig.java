package com.ledger.valuation.infrastructure.config;

import com.ledger.valuation.application.port.inbound.ProcessCommandUseCase;
import com.ledger.valuation.application.port.inbound.QueryAccountValueUseCase;
import com.ledger.valuation.application.port.inbound.RebuildReadSideUseCase;
import com.ledger.valuation.application.port.outbound.AccountValueProjectionPort;
import com.ledger.valuation.application.port.outbound.EventStorePort;
import com.ledger.valuation.application.service.CommandProcessingService;
import com.ledger.valuation.application.service.EventProjectionService;
import com.ledger.valuation.application.service.QueryAccountValueService;
import com.ledger.valuation.application.service.ReadSideRebuildService;
import com.ledger.valuation.domain.LedgerEventFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationBeanConfig {

    @Bean
    public LedgerEventFactory ledgerEventFactory() {
        return new LedgerEventFactory();
    }

    @Bean
    public EventProjectionService eventProjectionService(AccountValueProjectionPort projectionPort) {
        return new EventProjectionService(projectionPort);
    }

    @Bean
    public ProcessCommandUseCase processCommandUseCase(
            LedgerEventFactory eventFactory,
            EventStorePort eventStore,
            EventProjectionService projectionService
    ) {
        return new CommandProcessingService(eventFactory, eventStore, projectionService);
    }

    @Bean
    public QueryAccountValueUseCase queryAccountValueUseCase(AccountValueProjectionPort projectionPort) {
        return new QueryAccountValueService(projectionPort);
    }

    @Bean
    public RebuildReadSideUseCase rebuildReadSideUseCase(
            EventStorePort eventStore,
            EventProjectionService projectionService
    ) {
        return new ReadSideRebuildService(eventStore, projectionService);
    }
}
