package com.ledger.valuation.application.model;

import java.util.UUID;

public record CommitTransactionResult(
        Status status,
        UUID eventId,
        long resultingAccountValueMinorUnits
) {

    public enum Status {
        COMMITTED,
        ALREADY_PROCESSED
    }

    public static CommitTransactionResult committed(UUID eventId, long resultingAccountValueMinorUnits) {
        return new CommitTransactionResult(Status.COMMITTED, eventId, resultingAccountValueMinorUnits);
    }

    public static CommitTransactionResult alreadyProcessed(UUID eventId, long resultingAccountValueMinorUnits) {
        return new CommitTransactionResult(Status.ALREADY_PROCESSED, eventId, resultingAccountValueMinorUnits);
    }
}
