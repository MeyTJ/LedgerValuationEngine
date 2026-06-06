package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.RebuildPortfolioReadSideUseCase;
import com.ledger.valuation.application.port.outbound.EventStorePort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.PortfolioLedgerEvent;

import java.util.UUID;

public final class PortfolioReadSideRebuildService implements RebuildPortfolioReadSideUseCase {

    private final PortfolioEventStorePort portfolioEventStore;
    private final EventStorePort legacyEventStore;
    private final PortfolioLedgerEventProjectionService projectionService;

    public PortfolioReadSideRebuildService(
            PortfolioEventStorePort portfolioEventStore,
            EventStorePort legacyEventStore,
            PortfolioLedgerEventProjectionService projectionService
    ) {
        this.portfolioEventStore = portfolioEventStore;
        this.legacyEventStore = legacyEventStore;
        this.projectionService = projectionService;
    }

    @Override
    public void rebuildPortfolio(UUID portfolioId) {
        for (PortfolioLedgerEvent event : portfolioEventStore.loadStream(portfolioId).eventRecords()) {
            projectionService.project(event);
        }
    }

    @Override
    public void rebuildAll() {
        for (UUID aggregateId : legacyEventStore.listAggregateIds()) {
            try {
                rebuildPortfolio(aggregateId);
            } catch (RuntimeException ignored) {
                // Skip non-portfolio legacy aggregates during full rebuild scan
            }
        }
    }
}
