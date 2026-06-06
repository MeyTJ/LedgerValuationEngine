package com.ledger.valuation.domain;

import java.util.Objects;
import java.util.UUID;

public record OpenPortfolioCommand(
        String idempotencyToken,
        UUID portfolioId,
        String accountCode,
        String currency,
        String tenantId
) {

    public OpenPortfolioCommand {
        Objects.requireNonNull(idempotencyToken, "idempotencyToken");
        Objects.requireNonNull(portfolioId, "portfolioId");
        Objects.requireNonNull(accountCode, "accountCode");
        Objects.requireNonNull(currency, "currency");
        if (idempotencyToken.isBlank() || accountCode.isBlank() || currency.isBlank()) {
            throw new IllegalArgumentException("idempotencyToken, accountCode, and currency must not be blank");
        }
    }
}
