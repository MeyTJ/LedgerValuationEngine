package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.inbound.ApplyMarketTickUseCase;
import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.NormalizedMarketTick;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;
import com.ledger.valuation.domain.PortfolioNotFoundException;
import com.ledger.valuation.domain.ValuationPolicy;

import java.util.UUID;

public final class ApplyMarketTickService implements ApplyMarketTickUseCase {

    private final LedgerWriteUnitOfWorkPort unitOfWork;
    private final PortfolioEventStorePort eventStore;
    private final PortfolioLedgerEventFactory eventFactory;
    private final OutboxPort outbox;

    public ApplyMarketTickService(
            LedgerWriteUnitOfWorkPort unitOfWork,
            PortfolioEventStorePort eventStore,
            PortfolioLedgerEventFactory eventFactory,
            OutboxPort outbox
    ) {
        this.unitOfWork = unitOfWork;
        this.eventStore = eventStore;
        this.eventFactory = eventFactory;
        this.outbox = outbox;
    }

    @Override
    public void apply(UUID portfolioId, NormalizedMarketTick tick) {
        unitOfWork.execute(() -> {
            Portfolio portfolio = rehydrate(portfolioId);
            portfolio.ensureActive();
            long delta = ValuationPolicy.calculateMarkToMarketDeltaMinorUnits(
                    portfolio.accountValueMinorUnits(),
                    portfolio.accountValueMinorUnits(),
                    tick.priceMinorUnits()
            );
            PortfolioLedgerEvent event = eventFactory.createMarkToMarket(portfolio, tick, delta);
            eventStore.append(event);
            outbox.enqueue(event);
            return null;
        });
    }

    private Portfolio rehydrate(UUID portfolioId) {
        PortfolioLedgerEventStream stream = eventStore.loadStream(portfolioId);
        if (stream.isEmpty()) {
            throw new PortfolioNotFoundException(portfolioId);
        }
        return Portfolio.rehydrateFromEventRecords(stream.eventRecords());
    }
}
