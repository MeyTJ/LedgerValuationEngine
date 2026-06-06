package com.ledger.valuation.domain;

import java.time.Instant;
import java.util.UUID;

public sealed interface PortfolioLedgerEvent permits
        PortfolioLedgerEvent.PortfolioAccountOpened,
        PortfolioLedgerEvent.TransactionCommitted,
        PortfolioLedgerEvent.FeeAccrued,
        PortfolioLedgerEvent.InterestCredited,
        PortfolioLedgerEvent.AccountValueAdjustmentPosted {

    UUID eventId();

    UUID portfolioId();

    long sequenceNumber();

    Instant occurredAt();

    record PortfolioAccountOpened(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            String accountCode,
            String currency
    ) implements PortfolioLedgerEvent {}

    record TransactionCommitted(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long creditMinorUnits,
            long debitMinorUnits,
            String transactionReference
    ) implements PortfolioLedgerEvent {}

    record FeeAccrued(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long feeMinorUnits,
            String feeCategory
    ) implements PortfolioLedgerEvent {}

    record InterestCredited(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long interestMinorUnits,
            String interestPeriod
    ) implements PortfolioLedgerEvent {}

    record AccountValueAdjustmentPosted(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long accountValueDeltaMinorUnits,
            String adjustmentReason
    ) implements PortfolioLedgerEvent {}
}
