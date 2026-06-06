package com.ledger.valuation.application.service;

import com.ledger.valuation.application.model.CommitTransactionResult;
import com.ledger.valuation.application.port.inbound.CommitTransactionUseCase;
import com.ledger.valuation.application.port.outbound.LedgerWriteUnitOfWorkPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.CommitTransactionCommand;
import com.ledger.valuation.domain.Portfolio;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;
import com.ledger.valuation.domain.PortfolioNotFoundException;

import java.util.UUID;

public final class CommitTransactionCommandHandler implements CommitTransactionUseCase {

    private final LedgerWriteUnitOfWorkPort unitOfWork;
    private final PortfolioEventStorePort portfolioEventStore;
    private final PortfolioLedgerEventFactory eventFactory;

    public CommitTransactionCommandHandler(
            LedgerWriteUnitOfWorkPort unitOfWork,
            PortfolioEventStorePort portfolioEventStore,
            PortfolioLedgerEventFactory eventFactory
    ) {
        this.unitOfWork = unitOfWork;
        this.portfolioEventStore = portfolioEventStore;
        this.eventFactory = eventFactory;
    }

    @Override
    public CommitTransactionResult handle(CommitTransactionCommand command) {
        return unitOfWork.execute(() -> processWithinUnitOfWork(command));
    }

    private CommitTransactionResult processWithinUnitOfWork(CommitTransactionCommand command) {
        var existingTransaction = portfolioEventStore.findTransactionCommittedByIdempotencyToken(command.idempotencyToken());
        if (existingTransaction.isPresent()) {
            return resolveIdempotentReplay(existingTransaction.get(), command.portfolioId());
        }

        Portfolio portfolio = rehydratePortfolio(command.portfolioId());
        portfolio.ensureTransactionCommitPreservesNonNegativeAccountValue(
                command.creditMinorUnits(),
                command.debitMinorUnits()
        );

        PortfolioLedgerEvent.TransactionCommitted event = eventFactory.createTransactionCommitted(portfolio, command);
        portfolioEventStore.append(event);

        Portfolio resultingPortfolio = portfolio.applyCommittedTransaction(event);
        return CommitTransactionResult.committed(event.eventId(), resultingPortfolio.accountValueMinorUnits());
    }

    private CommitTransactionResult resolveIdempotentReplay(
            PortfolioLedgerEvent.TransactionCommitted existingTransaction,
            UUID expectedPortfolioId
    ) {
        if (!existingTransaction.portfolioId().equals(expectedPortfolioId)) {
            throw new IllegalStateException(
                    "idempotencyToken already bound to portfolio " + existingTransaction.portfolioId()
            );
        }

        Portfolio portfolio = rehydratePortfolio(existingTransaction.portfolioId());
        return CommitTransactionResult.alreadyProcessed(
                existingTransaction.eventId(),
                portfolio.accountValueMinorUnits()
        );
    }

    private Portfolio rehydratePortfolio(UUID portfolioId) {
        PortfolioLedgerEventStream stream = portfolioEventStore.loadStream(portfolioId);
        if (stream.isEmpty()) {
            throw new PortfolioNotFoundException(portfolioId);
        }
        return Portfolio.rehydrateFromEventRecords(stream.eventRecords());
    }
}
