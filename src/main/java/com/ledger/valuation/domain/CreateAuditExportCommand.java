package com.ledger.valuation.domain;

import java.util.Objects;
import java.util.UUID;

public record CreateAuditExportCommand(
        UUID portfolioId,
        String tenantId,
        long fromSequence,
        long toSequence
) {

    public CreateAuditExportCommand {
        Objects.requireNonNull(portfolioId, "portfolioId");
        Objects.requireNonNull(tenantId, "tenantId");
        if (tenantId.isBlank()) {
            throw new IllegalArgumentException("tenantId must not be blank");
        }
        if (fromSequence < 1L || toSequence < fromSequence) {
            throw new IllegalArgumentException("Invalid sequence range");
        }
    }
}
