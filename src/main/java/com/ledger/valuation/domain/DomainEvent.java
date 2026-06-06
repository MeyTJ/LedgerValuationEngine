package com.ledger.valuation.domain;

import java.time.Instant;
import java.util.UUID;

public sealed interface DomainEvent permits
        DomainEvent.AccountOpened,
        DomainEvent.TransactionPosted,
        DomainEvent.AccountValued {

    UUID eventId();
    UUID aggregateId();
    long sequenceNumber();
    Instant occurredAt();

    record AccountOpened(
            UUID eventId,
            UUID aggregateId,
            long sequenceNumber,
            Instant occurredAt,
            String accountCode,
            String currency
    ) implements DomainEvent {}

    record TransactionPosted(
            UUID eventId,
            UUID aggregateId,
            long sequenceNumber,
            Instant occurredAt,
            String debitAccount,
            String creditAccount,
            long amountMinorUnits
    ) implements DomainEvent {}

    record AccountValued(
            UUID eventId,
            UUID aggregateId,
            long sequenceNumber,
            Instant occurredAt,
            long balanceMinorUnits
    ) implements DomainEvent {}
}
