package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.RebuildPortfolioReadSideUseCase;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;

import java.util.UUID;

public final class PortfolioReadSideRebuildService implements RebuildPortfolioReadSideUseCase {

    private final PortfolioEventStorePort portfolioEventStore;
    private final PortfolioLedgerEventProjectionService projectionService;
    private final InstrumentPositionProjectionService positionProjectionService;

    public PortfolioReadSideRebuildService(
            PortfolioEventStorePort portfolioEventStore,
            PortfolioLedgerEventProjectionService projectionService,
            InstrumentPositionProjectionService positionProjectionService
    ) {
        this.portfolioEventStore = portfolioEventStore;
        this.projectionService = projectionService;
        this.positionProjectionService = positionProjectionService;
    }

    @Override
    public void rebuildPortfolio(UUID portfolioId) {
        for (var event : portfolioEventStore.loadStream(portfolioId).eventRecords()) {
            projectionService.project(event);
            positionProjectionService.project(event);
        }
    }

    @Override
    public void rebuildAll() {
        for (UUID portfolioId : portfolioEventStore.listPortfolioIds()) {
            rebuildPortfolio(portfolioId);
        }
    }
}
