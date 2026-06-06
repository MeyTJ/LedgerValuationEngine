package com.ledger.valuation.application.readmodel;

import java.time.Instant;
import java.util.UUID;

public record AccountValueDashboardView(
        UUID portfolioId,
        String accountCode,
        String tenantId,
        String currency,
        long accountValueMinorUnits,
        long lastSequenceNumber,
        Instant lastUpdatedAt
) {

    public AccountValueDashboardView withAccountValue(
            long nextAccountValueMinorUnits,
            long sequenceNumber,
            Instant updatedAt
    ) {
        return new AccountValueDashboardView(
                portfolioId,
                accountCode,
                tenantId,
                currency,
                nextAccountValueMinorUnits,
                sequenceNumber,
                updatedAt
        );
    }
}
