package com.ledger.valuation.domain;

import java.time.Clock;
import java.util.UUID;
import java.util.function.Supplier;

public final class LedgerEventFactory {

    private final Clock clock;
    private final Supplier<UUID> idSupplier;

    public LedgerEventFactory(Clock clock, Supplier<UUID> idSupplier) {
        this.clock = clock;
        this.idSupplier = idSupplier;
    }

    public LedgerEventFactory() {
        this(Clock.systemUTC(), UUID::randomUUID);
    }

    public DomainEvent createFrom(Command command) {
        var now = clock.instant();
        return switch (command) {
            case Command.OpenAccount open -> new DomainEvent.AccountOpened(
                    idSupplier.get(),
                    idSupplier.get(),
                    1L,
                    now,
                    open.accountCode(),
                    open.currency()
            );
            case Command.PostTransaction post -> new DomainEvent.TransactionPosted(
                    idSupplier.get(),
                    idSupplier.get(),
                    1L,
                    now,
                    post.debitAccount(),
                    post.creditAccount(),
                    post.amountMinorUnits()
            );
            case Command.ValueAccount value -> new DomainEvent.AccountValued(
                    idSupplier.get(),
                    idSupplier.get(),
                    1L,
                    now,
                    0L
            );
        };
    }
}
