package com.ledger.valuation.domain;

import java.time.Instant;
import java.util.UUID;

public sealed interface PortfolioLedgerEvent permits
        PortfolioLedgerEvent.PortfolioAccountOpened,
        PortfolioLedgerEvent.TransactionCommitted,
        PortfolioLedgerEvent.FeeAccrued,
        PortfolioLedgerEvent.InterestCredited,
        PortfolioLedgerEvent.AccountValueAdjustmentPosted,
        PortfolioLedgerEvent.MarkToMarketApplied,
        PortfolioLedgerEvent.FxRateCommitted,
        PortfolioLedgerEvent.AccountValueSnapshot,
        PortfolioLedgerEvent.PortfolioStatusChanged,
        PortfolioLedgerEvent.AccrualPosted,
        PortfolioLedgerEvent.PositionOpened,
        PortfolioLedgerEvent.PolicyEvaluated {

    UUID eventId();

    UUID portfolioId();

    long sequenceNumber();

    Instant occurredAt();

    default int schemaVersion() {
        return 1;
    }

    record PortfolioAccountOpened(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            String accountCode,
            String currency,
            String tenantId,
            PortfolioStatus status,
            String idempotencyToken
    ) implements PortfolioLedgerEvent {}

    record TransactionCommitted(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long creditMinorUnits,
            long debitMinorUnits,
            String transactionReference,
            String idempotencyToken
    ) implements PortfolioLedgerEvent {}

    record FeeAccrued(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long feeMinorUnits,
            String feeCategory,
            String accrualRunId
    ) implements PortfolioLedgerEvent {}

    record InterestCredited(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long interestMinorUnits,
            String interestPeriod,
            String accrualRunId
    ) implements PortfolioLedgerEvent {}

    record AccountValueAdjustmentPosted(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long accountValueDeltaMinorUnits,
            String adjustmentReason
    ) implements PortfolioLedgerEvent {}

    record MarkToMarketApplied(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            String instrumentId,
            long markToMarketDeltaMinorUnits,
            long markPriceMinorUnits,
            String valuationRunId
    ) implements PortfolioLedgerEvent {}

    record FxRateCommitted(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            String baseCurrency,
            String quoteCurrency,
            long rateMinorUnits,
            int rateScale
    ) implements PortfolioLedgerEvent {}

    record AccountValueSnapshot(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long accountValueMinorUnits,
            String currency
    ) implements PortfolioLedgerEvent {}

    record PortfolioStatusChanged(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            PortfolioStatus previousStatus,
            PortfolioStatus newStatus,
            String reason
    ) implements PortfolioLedgerEvent {}

    record AccrualPosted(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            long accrualMinorUnits,
            String accrualType,
            String accrualRunId
    ) implements PortfolioLedgerEvent {}

    record PositionOpened(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            String instrumentId,
            long quantityMinorUnits,
            long costBasisMinorUnits,
            String idempotencyToken
    ) implements PortfolioLedgerEvent {}

    record PolicyEvaluated(
            UUID eventId,
            UUID portfolioId,
            long sequenceNumber,
            Instant occurredAt,
            String ruleType,
            String decision,
            long evaluatedAmountMinorUnits,
            long thresholdMinorUnits
    ) implements PortfolioLedgerEvent {}
}
