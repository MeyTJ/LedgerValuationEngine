package com.ledger.valuation.application.service;

import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;
import com.ledger.valuation.domain.PortfolioNotFoundException;

import java.util.UUID;

public final class AccrualPostingService {

    private final LedgerWriteUnitOfWorkPort unitOfWork;
    private final PortfolioEventStorePort eventStore;
    private final PortfolioLedgerEventFactory eventFactory;
    private final OutboxPort outbox;

    public AccrualPostingService(
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

    public void postFee(UUID portfolioId, long feeMinorUnits, String feeCategory, String accrualRunId) {
        unitOfWork.execute(() -> {
            Portfolio portfolio = rehydrate(portfolioId);
            portfolio.ensureActive();
            PortfolioLedgerEvent event = eventFactory.createFeeAccrued(portfolio, feeMinorUnits, feeCategory, accrualRunId);
            eventStore.append(event);
            outbox.enqueue(event);
            return null;
        });
    }

    public void postInterest(UUID portfolioId, long interestMinorUnits, String interestPeriod, String accrualRunId) {
        unitOfWork.execute(() -> {
            Portfolio portfolio = rehydrate(portfolioId);
            portfolio.ensureActive();
            PortfolioLedgerEvent event = eventFactory.createInterestCredited(
                    portfolio, interestMinorUnits, interestPeriod, accrualRunId
            );
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
