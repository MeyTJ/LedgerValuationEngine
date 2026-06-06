package com.ledger.valuation.application.service;

import com.ledger.valuation.application.model.CommitTransactionResult;
import com.ledger.valuation.application.port.outbound.OutboxPort;
import com.ledger.valuation.application.port.outbound.PortfolioEventStorePort;
import com.ledger.valuation.domain.CommitTransactionCommand;
import com.ledger.valuation.domain.PortfolioLedgerEvent;
import com.ledger.valuation.domain.PortfolioLedgerEventFactory;
import com.ledger.valuation.domain.PortfolioLedgerEventStream;
import com.ledger.valuation.domain.PortfolioStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommitTransactionCommandHandlerTest {

    @Test
    void returnsAlreadyProcessedForDuplicateIdempotencyToken() {
        UUID portfolioId = UUID.randomUUID();
        Instant now = Instant.now();
        var existing = new PortfolioLedgerEvent.TransactionCommitted(
                UUID.randomUUID(), portfolioId, 2L, now, 500L, 0L, "tx", "dup-token"
        );
        var opened = new PortfolioLedgerEvent.PortfolioAccountOpened(
                UUID.randomUUID(), portfolioId, 1L, now, "ACC", "USD", "tenant", PortfolioStatus.ACTIVE
        );

        var handler = new CommitTransactionCommandHandler(
                operation -> operation.get(),
                new StubPortfolioEventStore(
                        new PortfolioLedgerEventStream(List.of(opened, existing)),
                        Optional.of(existing)
                ),
                new PortfolioLedgerEventFactory(),
                new NoOpOutbox()
        );

        CommitTransactionResult result = handler.handle(new CommitTransactionCommand(
                "dup-token", portfolioId, 100L, 0L, "ref"
        ));

        assertEquals(CommitTransactionResult.Status.ALREADY_PROCESSED, result.status());
    }

    @Test
    void rejectsInsufficientFunds() {
        UUID portfolioId = UUID.randomUUID();
        Instant now = Instant.now();
        var opened = new PortfolioLedgerEvent.PortfolioAccountOpened(
                UUID.randomUUID(), portfolioId, 1L, now, "ACC", "USD", "tenant", PortfolioStatus.ACTIVE
        );

        var handler = new CommitTransactionCommandHandler(
                operation -> operation.get(),
                new StubPortfolioEventStore(new PortfolioLedgerEventStream(List.of(opened)), Optional.empty()),
                new PortfolioLedgerEventFactory(),
                new NoOpOutbox()
        );

        assertThrows(com.ledger.valuation.domain.InsufficientFundsException.class, () ->
                handler.handle(new CommitTransactionCommand("token", portfolioId, 0L, 100L, "ref"))
        );
    }

    private static final class StubPortfolioEventStore implements PortfolioEventStorePort {
        private final PortfolioLedgerEventStream stream;
        private final Optional<PortfolioLedgerEvent.TransactionCommitted> existing;

        private StubPortfolioEventStore(
                PortfolioLedgerEventStream stream,
                Optional<PortfolioLedgerEvent.TransactionCommitted> existing
        ) {
            this.stream = stream;
            this.existing = existing;
        }

        @Override
        public PortfolioLedgerEventStream loadStream(UUID portfolioId) {
            return stream;
        }

        @Override
        public Optional<PortfolioLedgerEvent.TransactionCommitted> findTransactionCommittedByIdempotencyToken(
                String idempotencyToken
        ) {
            return existing;
        }

        @Override
        public void append(PortfolioLedgerEvent event) {}
    }

    private static final class NoOpOutbox implements OutboxPort {
        @Override
        public void enqueue(PortfolioLedgerEvent event) {}

        @Override
        public java.util.List<OutboxEntry> fetchPending(int limit) {
            return java.util.List.of();
        }

        @Override
        public void markPublished(UUID outboxId) {}

        @Override
        public void incrementRetry(UUID outboxId) {}
    }
}
