package com.ledger.valuation.application.readmodel;

import com.ledger.valuation.domain.Portfolio;

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

    public static AccountValueDashboardView fromPortfolio(Portfolio portfolio, Instant lastUpdatedAt) {
        return new AccountValueDashboardView(
                portfolio.portfolioId(),
                portfolio.accountCode(),
                portfolio.tenantId(),
                portfolio.accountValueCurrency(),
                portfolio.accountValueMinorUnits(),
                portfolio.lastSequenceNumber(),
                lastUpdatedAt
        );
    }
}
