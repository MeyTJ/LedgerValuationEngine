package com.ledger.valuation.domain;

import java.util.UUID;

public final class PortfolioNotFoundException extends RuntimeException {

    private final UUID portfolioId;

    public PortfolioNotFoundException(UUID portfolioId) {
        super("No event stream exists for portfolio " + portfolioId);
        this.portfolioId = portfolioId;
    }

    public UUID portfolioId() {
        return portfolioId;
    }
}
