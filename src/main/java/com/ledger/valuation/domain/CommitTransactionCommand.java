package com.ledger.valuation.domain;

import java.util.Objects;
import java.util.UUID;

public record CommitTransactionCommand(
        String idempotencyToken,
        UUID portfolioId,
        long creditMinorUnits,
        long debitMinorUnits,
        String transactionReference
) {

    public CommitTransactionCommand {
        Objects.requireNonNull(idempotencyToken, "idempotencyToken");
        if (idempotencyToken.isBlank()) {
            throw new IllegalArgumentException("idempotencyToken must not be blank");
        }
        Objects.requireNonNull(portfolioId, "portfolioId");
        Objects.requireNonNull(transactionReference, "transactionReference");
        if (transactionReference.isBlank()) {
            throw new IllegalArgumentException("transactionReference must not be blank");
        }
        if (creditMinorUnits < 0L) {
            throw new IllegalArgumentException("creditMinorUnits must be non-negative");
        }
        if (debitMinorUnits < 0L) {
            throw new IllegalArgumentException("debitMinorUnits must be non-negative");
        }
    }
}
