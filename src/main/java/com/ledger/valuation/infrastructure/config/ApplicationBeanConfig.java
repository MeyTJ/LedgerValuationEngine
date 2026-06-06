package com.ledger.valuation.infrastructure.config;

import com.ledger.valuation.application.port.inbound.CommitTransactionUseCase;
import com.ledger.valuation.application.port.inbound.ProcessCommandUseCase;
import com.ledger.valuation.application.port.inbound.QueryAccountValueDashboardUseCase;
import com.ledger.valuation.application.port.inbound.QueryAccountValueUseCase;
import com.ledger.valuation.application.port.inbound.RebuildReadSideUseCase;
import com.ledger.valuation.application.port.outbound.AccountValueProjectionPort;
import com.ledger.valuation.application.port.outbound.AccountValueReadModelPort;
import com.ledger.valuation.application.port.outbound.EventStorePort;
import com.ledger.valuation.application.port.outbound.LedgerEventCommittedPublisherPort;
import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.application.service.CommandProcessingService;
import com.ledger.valuation.application.service.CommitTransactionCommandHandler;
import com.ledger.valuation.application.service.EventProjectionService;
import com.ledger.valuation.application.service.PortfolioLedgerEventProjectionService;
import com.ledger.valuation.application.service.QueryAccountValueDashboardService;
import com.ledger.valuation.application.service.QueryAccountValueService;
import com.ledger.valuation.application.service.ReadSideRebuildService;
import com.ledger.valuation.domain.LedgerEventFactory;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationBeanConfig {

    @Bean
    public LedgerEventFactory ledgerEventFactory() {
        return new LedgerEventFactory();
    }

    @Bean
    public PortfolioLedgerEventFactory portfolioLedgerEventFactory() {
        return new PortfolioLedgerEventFactory();
    }

    @Bean
    public PortfolioLedgerEventProjectionService portfolioLedgerEventProjectionService(
            AccountValueReadModelPort readModel
    ) {
        return new PortfolioLedgerEventProjectionService(readModel);
    }

    @Bean
    public CommitTransactionUseCase commitTransactionUseCase(
            LedgerWriteUnitOfWorkPort unitOfWork,
            PortfolioEventStorePort portfolioEventStore,
            PortfolioLedgerEventFactory portfolioLedgerEventFactory,
            LedgerEventCommittedPublisherPort eventCommittedPublisher
    ) {
        return new CommitTransactionCommandHandler(
                unitOfWork,
                portfolioEventStore,
                portfolioLedgerEventFactory,
                eventCommittedPublisher
        );
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
    public QueryAccountValueUseCase queryAccountValueUseCase(AccountValueReadModelPort readModel) {
        return new QueryAccountValueService(readModel);
    }

    @Bean
    public QueryAccountValueDashboardUseCase queryAccountValueDashboardUseCase(
            AccountValueReadModelPort readModel
    ) {
        return new QueryAccountValueDashboardService(readModel);
    }

    @Bean
    public RebuildReadSideUseCase rebuildReadSideUseCase(
            EventStorePort eventStore,
            EventProjectionService projectionService
    ) {
        return new ReadSideRebuildService(eventStore, projectionService);
    }
}
