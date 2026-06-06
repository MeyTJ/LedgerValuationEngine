package com.ledger.valuation.domain;

import java.util.UUID;

public final class InsufficientFundsException extends RuntimeException {

    private final UUID portfolioId;
    private final long currentAccountValueMinorUnits;
    private final long projectedAccountValueMinorUnits;

    public InsufficientFundsException(
            UUID portfolioId,
            long currentAccountValueMinorUnits,
            long projectedAccountValueMinorUnits
    ) {
        super(
                "Transaction would drive Account Value below zero for portfolio "
                        + portfolioId
                        + "; currentAccountValueMinorUnits="
                        + currentAccountValueMinorUnits
                        + ", projectedAccountValueMinorUnits="
                        + projectedAccountValueMinorUnits
        );
        this.portfolioId = portfolioId;
        this.currentAccountValueMinorUnits = currentAccountValueMinorUnits;
        this.projectedAccountValueMinorUnits = projectedAccountValueMinorUnits;
    }

    public UUID portfolioId() {
        return portfolioId;
    }

    public long currentAccountValueMinorUnits() {
        return currentAccountValueMinorUnits;
    }

    public long projectedAccountValueMinorUnits() {
        return projectedAccountValueMinorUnits;
    }
}
