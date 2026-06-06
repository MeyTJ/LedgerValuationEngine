package com.ledger.valuation.domain;

import java.util.Objects;
import java.util.SequencedCollection;
import java.util.UUID;

public final class Portfolio {

    private final UUID portfolioId;
    private final String accountCode;
    private final AccountValue currentAccountValue;
    private final long lastSequenceNumber;

    private Portfolio(
            UUID portfolioId,
            String accountCode,
            AccountValue currentAccountValue,
            long lastSequenceNumber
    ) {
        this.portfolioId = Objects.requireNonNull(portfolioId, "portfolioId");
        this.accountCode = Objects.requireNonNull(accountCode, "accountCode");
        this.currentAccountValue = Objects.requireNonNull(currentAccountValue, "currentAccountValue");
        this.lastSequenceNumber = lastSequenceNumber;
    }

    public static Portfolio rehydrateFromEventRecords(SequencedCollection<PortfolioLedgerEvent> chronologicalEventRecords) {
        Objects.requireNonNull(chronologicalEventRecords, "chronologicalEventRecords");
        if (chronologicalEventRecords.isEmpty()) {
            throw new IllegalArgumentException("chronologicalEventRecords must not be empty");
        }

        Portfolio portfolio = null;
        for (PortfolioLedgerEvent eventRecord : chronologicalEventRecords) {
            portfolio = portfolio == null
                    ? openFromFirstEventRecord(eventRecord)
                    : portfolio.applyEventRecord(eventRecord);
        }
        return portfolio;
    }

    public UUID portfolioId() {
        return portfolioId;
    }

    public String accountCode() {
        return accountCode;
    }

    public AccountValue currentAccountValue() {
        return currentAccountValue;
    }

    public long accountValueMinorUnits() {
        return currentAccountValue.accountValueMinorUnits();
    }

    public String accountValueCurrency() {
        return currentAccountValue.currency();
    }

    public long lastSequenceNumber() {
        return lastSequenceNumber;
    }

    public AccountValue projectAccountValueAfterTransactionCommit(long creditMinorUnits, long debitMinorUnits) {
        if (creditMinorUnits < 0L) {
            throw new IllegalArgumentException("creditMinorUnits must be non-negative");
        }
        if (debitMinorUnits < 0L) {
            throw new IllegalArgumentException("debitMinorUnits must be non-negative");
        }
        return currentAccountValue.applyCredit(creditMinorUnits).applyDebit(debitMinorUnits);
    }

    public void ensureTransactionCommitPreservesNonNegativeAccountValue(long creditMinorUnits, long debitMinorUnits) {
        AccountValue projectedAccountValue = projectAccountValueAfterTransactionCommit(creditMinorUnits, debitMinorUnits);
        if (projectedAccountValue.accountValueMinorUnits() < 0L) {
            throw new InsufficientFundsException(
                    portfolioId,
                    currentAccountValue.accountValueMinorUnits(),
                    projectedAccountValue.accountValueMinorUnits()
            );
        }
    }

    public Portfolio applyCommittedTransaction(PortfolioLedgerEvent.TransactionCommitted committed) {
        return applyEventRecord(committed);
    }

    private static Portfolio openFromFirstEventRecord(PortfolioLedgerEvent eventRecord) {
        if (!(eventRecord instanceof PortfolioLedgerEvent.PortfolioAccountOpened opened)) {
            throw new IllegalStateException(
                    "First event record must be PortfolioAccountOpened, received: " + eventRecord.getClass().getSimpleName()
            );
        }
        return new Portfolio(
                opened.portfolioId(),
                opened.accountCode(),
                AccountValue.zero(opened.currency()),
                opened.sequenceNumber()
        );
    }

    private Portfolio applyEventRecord(PortfolioLedgerEvent eventRecord) {
        assertMonotonicSequence(eventRecord.sequenceNumber());
        assertPortfolioIdentity(eventRecord.portfolioId());

        AccountValue nextAccountValue = switch (eventRecord) {
            case PortfolioLedgerEvent.PortfolioAccountOpened ignored ->
                    throw new IllegalStateException("PortfolioAccountOpened is only valid as the first event record");
            case PortfolioLedgerEvent.TransactionCommitted committed ->
                    currentAccountValue
                            .applyCredit(committed.creditMinorUnits())
                            .applyDebit(committed.debitMinorUnits());
            case PortfolioLedgerEvent.FeeAccrued fee ->
                    currentAccountValue.applyDebit(fee.feeMinorUnits());
            case PortfolioLedgerEvent.InterestCredited interest ->
                    currentAccountValue.applyCredit(interest.interestMinorUnits());
            case PortfolioLedgerEvent.AccountValueAdjustmentPosted adjustment ->
                    currentAccountValue.applyAccountValueDelta(adjustment.accountValueDeltaMinorUnits());
        };

        return new Portfolio(portfolioId, accountCode, nextAccountValue, eventRecord.sequenceNumber());
    }

    private void assertMonotonicSequence(long incomingSequenceNumber) {
        if (incomingSequenceNumber <= lastSequenceNumber) {
            throw new IllegalStateException(
                    "Event record sequenceNumber must be strictly increasing; last="
                            + lastSequenceNumber
                            + ", incoming="
                            + incomingSequenceNumber
            );
        }
    }

    private void assertPortfolioIdentity(UUID incomingPortfolioId) {
        if (!portfolioId.equals(incomingPortfolioId)) {
            throw new IllegalStateException(
                    "Event record portfolioId mismatch; expected=" + portfolioId + ", incoming=" + incomingPortfolioId
            );
        }
    }
}
