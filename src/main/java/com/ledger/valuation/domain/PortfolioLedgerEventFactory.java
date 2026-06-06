package com.ledger.valuation.domain;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

public final class PortfolioLedgerEventFactory {

    private final Clock clock;
    private final Supplier<UUID> idSupplier;

    public PortfolioLedgerEventFactory(Clock clock, Supplier<UUID> idSupplier) {
        this.clock = clock;
        this.idSupplier = idSupplier;
    }

    public PortfolioLedgerEventFactory() {
        this(Clock.systemUTC(), UUID::randomUUID);
    }

    public PortfolioLedgerEvent.TransactionCommitted createTransactionCommitted(
            Portfolio portfolio,
            CommitTransactionCommand command
    ) {
        return new PortfolioLedgerEvent.TransactionCommitted(
                idSupplier.get(),
                portfolio.portfolioId(),
                portfolio.lastSequenceNumber() + 1L,
                clock.instant(),
                command.creditMinorUnits(),
                command.debitMinorUnits(),
                command.transactionReference(),
                command.idempotencyToken()
        );
    }
}
