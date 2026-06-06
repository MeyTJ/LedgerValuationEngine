package com.ledger.valuation.domain;

import java.util.Objects;
import java.util.UUID;

public record RegisterPositionCommand(
        String idempotencyToken,
        UUID portfolioId,
        String instrumentId,
        long quantityMinorUnits,
        long costBasisMinorUnits
) {

    public RegisterPositionCommand {
        Objects.requireNonNull(idempotencyToken, "idempotencyToken");
        Objects.requireNonNull(portfolioId, "portfolioId");
        Objects.requireNonNull(instrumentId, "instrumentId");
        if (idempotencyToken.isBlank() || instrumentId.isBlank()) {
            throw new IllegalArgumentException("idempotencyToken and instrumentId must not be blank");
        }
    }
}
