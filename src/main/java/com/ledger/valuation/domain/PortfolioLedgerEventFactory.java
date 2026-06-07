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

    public PortfolioLedgerEvent.PortfolioAccountOpened createPortfolioOpened(OpenPortfolioCommand command) {
        return new PortfolioLedgerEvent.PortfolioAccountOpened(
                idSupplier.get(),
                command.portfolioId(),
                1L,
                clock.instant(),
                command.accountCode(),
                command.currency(),
                command.tenantId(),
                PortfolioStatus.ACTIVE,
                command.idempotencyToken()
        );
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

    public PortfolioLedgerEvent.MarkToMarketApplied createMarkToMarket(
            Portfolio portfolio,
            NormalizedMarketTick tick,
            long markToMarketDeltaMinorUnits
    ) {
        return new PortfolioLedgerEvent.MarkToMarketApplied(
                idSupplier.get(),
                portfolio.portfolioId(),
                portfolio.lastSequenceNumber() + 1L,
                clock.instant(),
                tick.instrumentId(),
                markToMarketDeltaMinorUnits,
                tick.priceMinorUnits(),
                tick.valuationRunId()
        );
    }

    public PortfolioLedgerEvent.FxRateCommitted createFxRateCommitted(
            Portfolio portfolio,
            FxRate fxRate
    ) {
        return new PortfolioLedgerEvent.FxRateCommitted(
                idSupplier.get(),
                portfolio.portfolioId(),
                portfolio.lastSequenceNumber() + 1L,
                clock.instant(),
                fxRate.baseCurrency(),
                fxRate.quoteCurrency(),
                fxRate.rateMinorUnits(),
                fxRate.scale()
        );
    }

    public PortfolioLedgerEvent.AccountValueSnapshot createAccountValueSnapshot(Portfolio portfolio) {
        return new PortfolioLedgerEvent.AccountValueSnapshot(
                idSupplier.get(),
                portfolio.portfolioId(),
                portfolio.lastSequenceNumber() + 1L,
                clock.instant(),
                portfolio.accountValueMinorUnits(),
                portfolio.accountValueCurrency()
        );
    }

    public PortfolioLedgerEvent.FeeAccrued createFeeAccrued(
            Portfolio portfolio,
            long feeMinorUnits,
            String feeCategory,
            String accrualRunId
    ) {
        return new PortfolioLedgerEvent.FeeAccrued(
                idSupplier.get(),
                portfolio.portfolioId(),
                portfolio.lastSequenceNumber() + 1L,
                clock.instant(),
                feeMinorUnits,
                feeCategory,
                accrualRunId
        );
    }

    public PortfolioLedgerEvent.InterestCredited createInterestCredited(
            Portfolio portfolio,
            long interestMinorUnits,
            String interestPeriod,
            String accrualRunId
    ) {
        return new PortfolioLedgerEvent.InterestCredited(
                idSupplier.get(),
                portfolio.portfolioId(),
                portfolio.lastSequenceNumber() + 1L,
                clock.instant(),
                interestMinorUnits,
                interestPeriod,
                accrualRunId
        );
    }

    public PortfolioLedgerEvent.PolicyEvaluated createPolicyEvaluated(
            UUID portfolioId,
            long sequenceNumber,
            PolicyEvaluationResult result
    ) {
        return new PortfolioLedgerEvent.PolicyEvaluated(
                idSupplier.get(),
                portfolioId,
                sequenceNumber,
                clock.instant(),
                result.ruleType().name(),
                result.decision().name(),
                result.evaluatedAmountMinorUnits(),
                result.thresholdMinorUnits()
        );
    }
}
